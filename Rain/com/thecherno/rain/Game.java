package com.thecherno.rain;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

import com.thecherno.rain.graphics.Screen;
import com.thecherno.rain.input.Keyboard;

public class Game extends Canvas implements Runnable{
	private static final long serialVersionUID = 1L;
	
	public static int width = 300;
	public static int height = 168;
	public static int scale = 3;
	public static String title = "Rain";
	
	private Screen screen;
	private Keyboard keyboard;
	
	private Thread thread;
	private boolean running = false;
	private JFrame frame;
	
	private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	private int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
	
	public Game() {
		Dimension size = new Dimension(width * scale, height * scale);
		setPreferredSize(size);
		
		screen = new Screen(width, height);
		frame = new JFrame();
		keyboard = new Keyboard();

		
		addKeyListener(keyboard);	
	}
	
	public synchronized void start() {
		running = true;
		thread = new Thread(this, "Display");
		thread.start();
	}

	public synchronized void stop() {
		running = false;
		try{
			thread.join();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// FPS Counter
		long lastTime = System.nanoTime();
		long timer = System.currentTimeMillis();
		final double ns = 1_000_000_000.0 / 60.0;
		double delta = 0;
		int frames = 0;
		int updates = 0;
		requestFocus();
		while(running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while(delta >= 1) {
				update();
				updates++;
				delta--;
			}
			render();
			frames++;
			
			if(System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				System.out.println(updates+ " ups, " +frames+  " fps");	
				frame.setTitle(title+ " | " +updates+ " ups, " +frames+ " fps");
				 
				updates = 0;
				frames = 0;
				
			}
		}
		stop();
	}
	
	int x = 0, y = 0;
	public void update() {
		
		// Keyboard class
		keyboard.update();
		if(keyboard.up) y--;
		if(keyboard.down) y++;
		if(keyboard.left) x--;
		if(keyboard.right) x++;
	
	}
	
	//For Rendering images with 3
	
	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(3);
			return;
		}
		
		//renders pixels from screen object
		screen.clear();
		screen.render(x, y);
		
		
		for(int i = 0; i < pixels.length; i++) {
			pixels[i] = screen.pixels[i];
		}
		
		Graphics g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
		g.dispose();
		bs.show();
	}
	
	// Main method 
	public static void main(String args[]) {
		Game game = new Game();
		game.frame.setResizable(false);
		game.frame.setTitle(Game.title);
		game.frame.add(game);
		game.frame.pack();
		game.frame.requestFocusInWindow();
		game.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		game.frame.setLocationRelativeTo(null);
		game.frame.setVisible(true);
		
		game.start();
	}
	
}

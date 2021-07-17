package rasterizeImage;

import java.awt.Dimension;

import javax.swing.JFrame;

public class Main {

	private static JFrame frame;

	public static void main(String[] args) {

		// creating a jFrame
		frame = new JFrame("Rasterize an Image");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// add the program panel to the frame
		RasterizeImage rasterizeImage = new RasterizeImage();
		frame.add(rasterizeImage);

		// show the jFrame
		frame.setVisible(true);

		while (true) {

			try {
				Thread.sleep(20);
				frame.repaint();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public static JFrame getFrame() {
		return frame;
	}
}

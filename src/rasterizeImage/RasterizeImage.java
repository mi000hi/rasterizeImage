package rasterizeImage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;

public class RasterizeImage extends JPanel implements ActionListener, MouseListener {

	private final Font FONT_BUTTON = new Font("Calibri", Font.PLAIN, 24);
	private final Font FONT_LABEL = new Font("Calibri", Font.BOLD | Font.ITALIC, 12);
	private final Point IMAGE_LOCATION = new Point(50, 220);
	private JFileChooser fileChooser = new JFileChooser();

	private final double IMAGE_ASPECT_RATIO = 16.0 / 9.0;
	private double IMAGE_SCALE_RATIO = 1.0;
	private Point rasterPosition_d1 = new Point(0, 0);
	private boolean movableRaster = true;
	private Point rasterPosition = new Point(0, 0);

	private BufferedImage image;
	private JTextField tf_rasterWidth;
	private JTextField tf_rasterRowOffset, tf_rasterColumnOffset;
	private JTextField tf_rasterColumns, tf_rasterRows;
	private JTextField tf_rasterPadding;

	/**
	 * creates a panel containing a canvas and some GUI elements to manipulate an
	 * image
	 */
	public RasterizeImage() {

		// superconstructor
		super();

		// set up the layout for the panel
		SpringLayout panelLayout = new SpringLayout();
		SpringLayout.Constraints panelConstraints = panelLayout.getConstraints(this);

		// panel properties
		this.setLayout(panelLayout);
		this.setOpaque(true);
		this.addMouseListener(this);
		this.setMinimumSize(new Dimension(1920+100, 500));
		Main.getFrame().setMinimumSize(this.getMinimumSize());

		// add GUI components
		addGuiComponents(panelLayout);
	}

	/**
	 * adds the GUI components to the main panel
	 * 
	 * @param panelLayout the used SpringLayout
	 */
	private void addGuiComponents(SpringLayout panelLayout) {

		// add a button to load an image
		JButton btn_loadImage = new JButton("load an image");
		btn_loadImage.addActionListener(this);
		btn_loadImage.setFont(FONT_BUTTON);
		btn_loadImage.setActionCommand("load image");
		addComponent(this, btn_loadImage, 50, 50, 300, 50);

		// add a button to cut the image into subimages
		JButton btn_cutImage = new JButton("cut the image");
		btn_cutImage.addActionListener(this);
		btn_cutImage.setFont(FONT_BUTTON);
		btn_cutImage.setActionCommand("cut image");
		addComponent(this, btn_cutImage, 400, 50, 300, 50);

		// add a textfield to define the rasterwidth
		tf_rasterWidth = new JTextField("1500");
		tf_rasterWidth.setFont(FONT_BUTTON);
		addComponentWithLabel(this, "raster width", tf_rasterWidth, 50, 120, 200, 50);

		// add a textfield to define the offset of a rasterrow
		tf_rasterRowOffset = new JTextField("0");
		tf_rasterRowOffset.setFont(FONT_BUTTON);
		addComponentWithLabel(this, "raster row offset", tf_rasterRowOffset, 300, 120, 200, 50);

		// add a textfield to define the offset of a rastercolumn
		tf_rasterColumnOffset = new JTextField("0");
		tf_rasterColumnOffset.setFont(FONT_BUTTON);
		addComponentWithLabel(this, "raster column offset", tf_rasterColumnOffset, 550, 120, 200, 50);

		// add a textfield to define the number of raster columns
		tf_rasterColumns = new JTextField("3");
		tf_rasterColumns.setFont(FONT_BUTTON);
		addComponentWithLabel(this, "# raster columns", tf_rasterColumns, 800, 120, 200, 50);

		// add a textfield to define the number of row columns
		tf_rasterRows = new JTextField("3");
		tf_rasterRows.setFont(FONT_BUTTON);
		addComponentWithLabel(this, "# raster rows", tf_rasterRows, 1050, 120, 200, 50);

		// add a textfield to define the padding between rasterfields
		tf_rasterPadding = new JTextField("0");
		tf_rasterPadding.setFont(FONT_BUTTON);
		addComponentWithLabel(this, "raster padding", tf_rasterPadding, 1300, 120, 200, 50);
	}

	/**
	 * adds a component to the specified panel using the provided SpringLayout
	 * 
	 * @param parent    the parent jpanel of the component, needed to receive the
	 *                  SpringLayout
	 * @param component the component to add to the parent jpanel
	 * @param x         the x-position for the component
	 * @param y         the y-position for the component
	 * @param width     the width of the component
	 * @param height    the height of the component
	 */
	private void addComponent(JPanel parent, Component component, int x, int y, int width, int height) {

		SpringLayout parentLayout = (SpringLayout) parent.getLayout();
		SpringLayout.Constraints constraints = parentLayout.getConstraints(component);

		// set x and y coordinates
		constraints.setX(Spring.constant(x));
		constraints.setY(Spring.constant(y));

		// set width and height of the component
		constraints.setWidth(Spring.constant(width));
		constraints.setHeight(Spring.constant(height));

		// add the component to the panel
		parent.add(component, constraints);
	}

	/**
	 * adds a component to the specified panel using the provided SpringLayout
	 * and also adds a label on the top of the component. the label is not included
	 * in the specified dimensions (position and size)
	 * 
	 * @param parent    the parent jpanel of the component, needed to receive the
	 *                  SpringLayout
	 * @param label		the label of the component
	 * @param component the component to add to the parent jpanel
	 * @param x         the x-position for the component
	 * @param y         the y-position for the component
	 * @param width     the width of the component
	 * @param height    the height of the component
	 */
	private void addComponentWithLabel(JPanel parent, String label, Component component, int x, int y, int width,
			int height) {

		// create the label
		JLabel l = new JLabel(label);
		l.setFont(FONT_LABEL);
		
		// add the label
		addComponent(parent, l, x, y-17, width, 20);
		
		// add the component
		addComponent(parent, component, x, y, width, height);
	}

	/**
	 * paints the image and a raster onto the panel
	 */
	public void paintComponent(Graphics g) {

		// clears the image
		super.paintComponent(g);

		if (image == null)
			return;

		// paint our image
		int imageWidth = 1920;
		int imageHeight = imageWidth * image.getHeight() / image.getWidth();
		g.drawImage(image, IMAGE_LOCATION.x, IMAGE_LOCATION.y, imageWidth, imageHeight, this);

		// set size of the panel
		this.setMinimumSize(new Dimension(2 * 50 + imageWidth, 30 + 220 + imageHeight + 50));
		Main.getFrame().setMinimumSize(this.getMinimumSize());

		// draw the raster onto the panel over the image
		int rasterWidth = Math.max(1, readIntFromTextField(tf_rasterWidth));
		int rasterRowOffset = readIntFromTextField(tf_rasterRowOffset);
		int rasterColumnOffset = readIntFromTextField(tf_rasterColumnOffset);
		int rasterColumns = Math.max(1, readIntFromTextField(tf_rasterColumns));
		int rasterRows = Math.max(1, readIntFromTextField(tf_rasterRows));
		rasterPosition.setLocation(
				(this.getMousePosition() != null && movableRaster) ? this.getMousePosition() : rasterPosition);
		drawRaster((Graphics2D) g, rasterPosition, rasterColumns, rasterRows, rasterWidth, rasterRowOffset,
				rasterColumnOffset);
	}

	/**
	 * draws a raster on the specified location with the specified amount of rows
	 * and columns
	 * 
	 * @param position           location of the top left corner of the raster
	 * @param numColumns         number of columns in the raster
	 * @param numRows            number of rows in the raster
	 * @param rasterWidth        total width of the raster
	 * @param rasterRowOffset    the offset of a rasterrow in pixels
	 * @param rasterColumnOffset the offset of a rastercolumn in pixels
	 */
	private void drawRaster(Graphics2D g2, Point position, int numColumns, int numRows, int rasterWidth,
			int rasterRowOffset, int rasterColumnOffset) {

		// safe the current stroke
		Stroke oldStroke = g2.getStroke();

		int x1, x2, y1, y2;
		Point p;
		int rasterfieldWidth = (int) (rasterWidth / numColumns);
		int rasterfieldHeight = (int) (rasterfieldWidth / IMAGE_ASPECT_RATIO);

		// horizontal lines
		BasicStroke stroke = new BasicStroke(5);
		g2.setStroke(stroke);
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numColumns; j++) {

				p = getRasterfieldLocation(position, j, i);
				g2.setColor(Color.RED);
				g2.drawRect(p.x, p.y, rasterfieldWidth, rasterfieldHeight);
				g2.setColor(new Color(1f, 0f, 0f, 0.2f));
				g2.fillRect(p.x, p.y, rasterfieldWidth, rasterfieldHeight);
			}
		}

		// restore the stroke
		g2.setStroke(oldStroke);

	}

	/**
	 * reads an integer from the specified JTextField. Value is 0 if textfield does
	 * not contain a number
	 * 
	 * @param textField the textfield to read the integer from
	 * @return the integer inside the textfield, 0 if there is no number
	 */
	private int readIntFromTextField(JTextField textField) {

		// read from the textfield
		String text = textField.getText();

		try {
			int num = Integer.parseInt(text);
			return num;
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * returns the top left corner position of a rasterfield located at (x,y) in the
	 * raster
	 * 
	 * @param position the top left corner of the (0,0) rasterfield
	 * @param x        x-position in the raster
	 * @param y        y-position in the raster
	 * @return top left corner of the rasterfield in pixels relative to the paint
	 *         origin
	 */
	private Point getRasterfieldLocation(Point position, int x, int y) {
		int xPos = (int) (position.x + y * readIntFromTextField(tf_rasterRowOffset)
				+ x * readIntFromTextField(tf_rasterWidth) / Math.max(1, readIntFromTextField(tf_rasterColumns))
				+ x * readIntFromTextField(tf_rasterPadding));
		int yPos = (int) (position.y
				+ x * readIntFromTextField(tf_rasterColumnOffset) + y * readIntFromTextField(tf_rasterWidth)
						/ Math.max(1, readIntFromTextField(tf_rasterColumns)) / IMAGE_ASPECT_RATIO
				+ y * readIntFromTextField(tf_rasterPadding));

		return new Point(xPos, yPos);

	}

	/**
	 * Override from ActionListener
	 */
	@Override
	public void actionPerformed(ActionEvent a) {

		switch (a.getActionCommand()) {

		case "load image":
			// let the user choose an image
			int chooser = fileChooser.showOpenDialog(this);
			if (chooser != JFileChooser.APPROVE_OPTION) {
				System.err.println("User did not choose a new file");
				return;
			}
			File file = fileChooser.getSelectedFile();
			try {
				image = ImageIO.read(file);
				IMAGE_SCALE_RATIO = image.getWidth() / 1920.0;

				this.repaint();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;

		case "cut image":

			// test if an image is selected
			if (image == null) {
				System.err.println("No image selected!");
				return;
			}
			
			// ask the user where to save the files  
			int userSelection = fileChooser.showSaveDialog(this);
			if (userSelection != JFileChooser.APPROVE_OPTION) {
				System.err.println("User did not select a save destination. Subimages were not saved!");
				return;
			}
			File destinationFile = fileChooser.getSelectedFile();

			// save all subimages
			int numRows = readIntFromTextField(tf_rasterRows);
			int numColumns = readIntFromTextField(tf_rasterColumns);
			BufferedImage subimage;
			Point subimageLocation;
			int subimageWidth = (int) (readIntFromTextField(tf_rasterWidth) * IMAGE_SCALE_RATIO / numColumns);
			int subimageHeight = (int) (subimageWidth / IMAGE_ASPECT_RATIO);
			File subimageFile;

			for (int i = 0; i < numRows; i++) {
				for (int j = 0; j < numColumns; j++) {

					// adjust the subimagelocation to match the real imagecoordinates
					subimageLocation = getRasterfieldLocation(rasterPosition, j, i);
					subimageLocation.setLocation(subimageLocation.x - IMAGE_LOCATION.x,
							subimageLocation.y - IMAGE_LOCATION.y);
					subimageLocation.setLocation(subimageLocation.x * IMAGE_SCALE_RATIO,
							subimageLocation.y * IMAGE_SCALE_RATIO);

					// retrieve the subimage
					try {
						subimage = image.getSubimage(subimageLocation.x, subimageLocation.y, subimageWidth,
								subimageHeight);

						// save the subimage in a new file
						subimageFile = new File(destinationFile.getParent() + "/" + destinationFile.getName().split("\\.")[0] + "_(" + j + i + ").png");

						ImageIO.write(subimage, "png", subimageFile);
						System.out.println("Subimage saved under [" + subimageFile.getAbsolutePath() + "]");
					} catch (RasterFormatException rfe) {
						System.err.println("Subimage (" + j + ", " + i
								+ ") is not fully contained in the original image. It will not be saved!");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			break;

		}

	}

	/**
	 * Override from MouseListener
	 */
	@Override
	public void mouseClicked(MouseEvent m) {

		// toggle the raster movable or stationary
		movableRaster = !movableRaster;

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

}

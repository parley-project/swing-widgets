package us.bringardner.swing.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;


public class GlassPane extends JComponent implements MouseListener {
	private static final long serialVersionUID = 1L;
	static BufferedImage image; 
	static {
		try {
			image = ImageIO.read(GlassPane.class.getResourceAsStream("/us/bringardner/swing/icons/Globe100x100.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static BufferedImage rotateImage(BufferedImage buffImage, double angle) {
		double radian = Math.toRadians(angle);
		double sin = Math.abs(Math.sin(radian));
		double cos = Math.abs(Math.cos(radian));

		int width = buffImage.getWidth();
		int height = buffImage.getHeight();

		int nWidth = (int) Math.floor((double) width * cos + (double) height * sin);
		int nHeight = (int) Math.floor((double) height * cos + (double) width * sin);

		BufferedImage rotatedImage = new BufferedImage(
				nWidth, nHeight, BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphics = rotatedImage.createGraphics();

		graphics.setRenderingHint(
				RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		graphics.translate((nWidth - width) / 2, (nHeight - height) / 2);
		// rotation around the center point
		graphics.rotate(radian, (double) (width / 2), (double) (height / 2));
		graphics.drawImage(buffImage, 0, 0, null);
		graphics.dispose();

		return rotatedImage;
	}





	private Rectangle imgBounds;
	private boolean pressed = false;
	int delay = 100; //milliseconds
	int angle = 0;
	BufferedImage rotated = image;

	
	Runnable blink = new Runnable() {

		@Override
		public void run() {
			try {
				System.out.println("Start blink");
				while(isVisible()) {
					Thread.sleep(delay);
					if( angle !=0) {
						rotated = rotateImage(image, angle);					
					}
					//System.out.println("before blink");
					SwingUtilities.invokeAndWait(()->repaint());
					//System.out.println("after blink");
					
					if( (angle+=10)>=360) {
						angle = 0;
					}		
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			System.out.println("Stop blink");
		}
	};

	public GlassPane() {
		super();
		addMouseListener(this);
	}

	@Override
	public void setVisible(boolean aFlag) {				
		super.setVisible(aFlag);
		if( aFlag) {
			new Thread(blink).start();
		} 
	}

	protected void paintComponent(Graphics g) {
		Rectangle b = getBounds();
		double cx = b.getCenterX();
		double cy = b.getCenterY();
		int w = rotated.getWidth();
		int h = rotated.getHeight();

		int x = (int)cx-(rotated.getWidth()/2);
		int y = (int)cy-(rotated.getHeight()/2);
		if( pressed ) {
			g.setColor(Color.white);
			g.fillOval(x, y, w+10, h+10);
		}
		imgBounds = new Rectangle(x,y, rotated.getWidth(), rotated.getHeight());
		g.drawImage(rotated, x,y, null);
		
	}


	public void mouseReleased(MouseEvent e) {
		e.consume();
		if( imgBounds!=null && imgBounds.contains(e.getPoint())) {
			if( pressed ) {
				setVisible(false);
			}
			pressed = false;
			repaint();
		}			    
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		e.consume();				
	}

	@Override
	public void mousePressed(MouseEvent e) {
		e.consume();
		if( imgBounds!=null && imgBounds.contains(e.getPoint())) {
			System.out.println("Pressed");
			pressed = true;
			repaint();
		}


	}

	@Override
	public void mouseEntered(MouseEvent e) {
		e.consume();

	}

	@Override
	public void mouseExited(MouseEvent e) {
		e.consume();
	}




}

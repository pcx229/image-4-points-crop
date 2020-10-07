package ui;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * shows a circle with pixel level close look at the image for a specific focusing point.
 */
class CloseLook extends JPanel {

	private static final int arrowLength = 5;
	private static final int borderSize = 5;
	
	private BufferedImage image;
	private Point2D focus;

	public CloseLook() {
		this(null);
	}

	public CloseLook(BufferedImage image) {
		setOpaque(false);
		if(image != null) {
			this.image = image;
		}
	}
	
	public void setImage(BufferedImage image) {
		this.image = image;
	}
	
	public void removeImage() {
		image = null;
		focus = null;
	}
	
	public boolean hasImage() {
		return image != null;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public void setFocusPoint(Point2D focus) {
		this.focus = focus;
	}
	
    @Override
    protected void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	
    	if(image == null || focus == null) {
    		return;
    	}
    	
    	int width = getWidth(), height = getHeight();
    	
    	// smooth
		((Graphics2D)g).setRenderingHint(
	            RenderingHints.KEY_ANTIALIASING,
	            RenderingHints.VALUE_ANTIALIAS_ON);
		
		// border
    	g.setColor(Color.lightGray);
    	g.fillOval(0, 0, width, height);
		
    	// peek image
        g.setClip(new Ellipse2D.Float(borderSize, borderSize, width-borderSize*2, height-borderSize*2));
        g.drawImage(image, (int)(width/2-focus.getX()), (int)(height/2-focus.getY()), image.getWidth(), image.getHeight(), this);

        // hide edge
        g.setClip(null);
    	g.setColor(Color.lightGray);
        ((Graphics2D)g).setStroke(new BasicStroke(2f));
        g.drawOval(borderSize, borderSize, width-borderSize*2, height-borderSize*2);
        
        // middle arrow
        Color c = null;
        if((focus.getX() > 0 && focus.getX() < image.getWidth()) &&
        		(focus.getY() > 0 && focus.getY() < image.getHeight())) {
        	c = new Color(image.getRGB((int)focus.getX(), (int)focus.getY()));
        	// different color then the middle for better contrast
        	c = new Color(255-c.getRed(), 255-c.getGreen(), 255-c.getBlue());
        } else {
        	c = Color.black;
        }
    	g.setColor(c);
        ((Graphics2D)g).setStroke(new BasicStroke(1f));
        g.drawLine(width/2, height/2-arrowLength/2, width/2, height/2+arrowLength/2);
        g.drawLine(width/2-arrowLength/2, height/2, width/2+arrowLength/2, height/2);
    }
}

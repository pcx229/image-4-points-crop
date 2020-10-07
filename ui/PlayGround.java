package ui;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import ui.Area.changesInAreaListener;

/**
 * show an image with a selection area and a closer look window.
 */
class PlayGround extends JPanel {
	
	private ImageViewerWorld background;
	private Area selection;
	private boolean selection_area_visable;
	private boolean image_is_loaded;
	private CloseLook peek;
	private static final int peek_height = 100, peek_width = 100;
	private boolean close_peek_visable;
	
	public PlayGround() {
		this(null, null);
	}
	
	public PlayGround(BufferedImage image, Point2D[] coords) {
		setLayout(null);
		if(image != null) {
			image_is_loaded = true;
		} else {
			image_is_loaded = false;
		}
		// image viewer
		background = new ImageViewerWorld(image);
		add(background);
		// selection area
		selection = new Area(coords);
		selection.setLocationRelative(background);
		if(image_is_loaded) {
			selection.setVisible(true);
			selection_area_visable = true;
		} else {
			selection.setVisible(false);
			selection_area_visable = false;
		}
		add(selection);
		setComponentZOrder(selection, 0);
		// close look
		peek = new CloseLook();
		peek.setVisible(false);
		close_peek_visable = false;
		peek.setSize(peek_height, peek_width);
		add(peek);
		setComponentZOrder(peek, 0);
		selection.addChangesInAreaListener(new changesInAreaListener() {
			
			@Override
			public void onStart() {
				peek.setFocusPoint(null);
				peek.repaint();
			}
			
			@Override
			public void onProgress(Point2D[] dots, Point2D changed) {
				if(background.translatePoint(changed).getX() < getWidth()/2) {
					peek.setLocation(getWidth()-peek_width, 0);
				} else {
					peek.setLocation(0, 0);
				}
				peek.setFocusPoint(changed);
			}
			
			@Override
			public void onEnd() {
				peek.setFocusPoint(null);
				peek.repaint();
			}
		});
		// size changes
    	addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {}
			@Override
			public void componentResized(ComponentEvent e) {
				background.setSize(getSize());
				selection.setSize(getSize());
			}
			@Override
			public void componentMoved(ComponentEvent e) {}
			@Override
			public void componentHidden(ComponentEvent e) {}
		});
    	// direct mouse events to background
		addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				background.dispatchEvent(e);
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				background.dispatchEvent(e);
			}
		});
		addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				background.dispatchEvent(e);
			}
		});
        addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {}
			
			@Override
			public void mousePressed(MouseEvent e) {}
			
			@Override
			public void mouseExited(MouseEvent e) {
				background.dispatchEvent(e);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				background.dispatchEvent(e);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {}
		});
	}
	
	public void reset() {
		background.reset();
		selection.reset();
	}
	
	public void setImage(BufferedImage image) {
		image_is_loaded = true;
		background.setImage(image);
		selection.setVisible(selection_area_visable);
		selection.reset();
		peek.setVisible(close_peek_visable);
		peek.setImage(background.getImage());
	}
	
	public void removeImage() {
		image_is_loaded = false;
		background.removeImage();
		selection.setVisible(false);
		peek.removeImage();
		peek.setVisible(false);
	}
	
	public boolean hasImage() {
		return background.hasImage();
	}
	
	public ImageViewerWorld getWorldImageBackground() {
		return background;
	}
	
	public void setStable(boolean stable) {
		background.setStable(stable);
		selection.reset();
	}
	
	public void setSelectionAreaVisable(boolean visable) {
		selection_area_visable = visable;
		if(selection_area_visable && image_is_loaded) {
			selection.setVisible(true);
			selection.reset();
		} else {
			selection.setVisible(false);
		}
	}
	
	public void setCloseLookVisable(boolean visable) {
		close_peek_visable = visable;
		if(close_peek_visable && image_is_loaded) {
			peek.setVisible(true);
		} else {
			peek.setVisible(false);
		}
	}
	
	public Point2D[] getCoords() {
		return selection.getCoords();
	}
	
	public void setCoords(int[][] area) {
		selection.setCoords(area);
	}
	
    public void setImageNotLoadedMessage(String msg) {
    	background.setImageNotLoadedMessage(msg);
    }
}

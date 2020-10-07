package ui;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * 2D canvas that the user can interact with by mouse events.<br>
 * dragging the area and scaling on the cursor position is supported, any class that
 * inherit can override the regular paint function paintComponent(Graphics g)
 * to paint on the canvas, but should call super.paintComponent() first.
 */
abstract class World2D extends JPanel {
	
	public static final double MIN_ZOOM = 0.3, MAX_ZOOM = 5, NO_ZOOM = 1, LEAP_ZOOM = 0.2;
    protected double PIVOT_ZOOM = 1;
	protected double zoom;
	protected double offset_x, offset_y;

    public World2D() {
    	this(NO_ZOOM, 0, 0);
    }

    public World2D(double zoom, double offset_x, double offset_y) {
    	setLayout(null);
    	this.zoom = zoom;
    	this.offset_x = offset_x;
    	this.offset_y = offset_y;
    	// reset area when container changed
    	addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {}
			@Override
			public void componentResized(ComponentEvent e) {
				reset();
			}
			@Override
			public void componentMoved(ComponentEvent e) {}
			@Override
			public void componentHidden(ComponentEvent e) {}
		});
        addHierarchyBoundsListener(new HierarchyBoundsListener() {
 			@Override
 			public void ancestorResized(HierarchyEvent e) {
 				reset();
 			}
 			@Override
 			public void ancestorMoved(HierarchyEvent arg0) {}
 	   });
        // zooming
        addMouseWheelListener(new MouseWheelListener() {
 			@Override
 			public void mouseWheelMoved(MouseWheelEvent e) {
 				zoom(e.getPoint(), e.getWheelRotation());
 			}
        });
        // dragging
        addMouseMotionListener(new MouseMotionListener() {
 			@Override
 			public void mouseMoved(MouseEvent e) {
 				startDragging(e.getPoint());
 				notifyAllMousePositionChangeListeners(backPoint(e.getPoint()));
 			}
 			@Override
 			public void mouseDragged(MouseEvent e) {
 				dragging(e.getPoint());
 			}
 	   });
        // notify mouse position is out of container
        addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {}
			
			@Override
			public void mousePressed(MouseEvent e) {}
			
			@Override
			public void mouseExited(MouseEvent e) {
 				notifyAllMousePositionChangeListeners(null);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
 				notifyAllMousePositionChangeListeners(null);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {}
		});
    }
    
    // world zoom and position listeners
    
    public interface prespectiveChangeListener {
    	void onChangePosition(double offset_x, double offset_y);
    	void onChangeZoom(double zoom);
    }
    
    private List<prespectiveChangeListener> prespective_listeners = new ArrayList<>();
    
    public void addPrespectiveListener(prespectiveChangeListener listener) {
    	prespective_listeners.add(listener);
    }
    
    public void removePrespectiveListener(prespectiveChangeListener listener) {
    	prespective_listeners.remove(listener);
    }
    
    private void notifyAllPrespectivePositionListeners() {
    	for(prespectiveChangeListener p : prespective_listeners) {
    		p.onChangePosition(offset_x, offset_y);
    	}
    }
    
    public void notifyAllPrespectiveZoomListeners() {
    	for(prespectiveChangeListener i : prespective_listeners) {
    		i.onChangeZoom(zoom);
    	}
    }
    
    // mouse position in world listener
    
    public interface mousePositionListener {
    	void onChange(Point2D p);
    }
    
    private List<mousePositionListener> mouse_position_listeners = new ArrayList<>();
    
    public void addMousePositionListener(mousePositionListener listener) {
    	mouse_position_listeners.add(listener);
    }
    
    public void removeousePositionListener(mousePositionListener listener) {
    	mouse_position_listeners.remove(listener);
    }
    
    private void notifyAllMousePositionChangeListeners(Point2D p) {
    	for(mousePositionListener i : mouse_position_listeners) {
    		i.onChange(p);
    	}
    }
    
    public void reset() {
    	zoom(NO_ZOOM);
    	offset(0, 0);
    }

    // halt stops both dragging and resizing
    
    private boolean halt = false;

    public void setHalt(boolean enabled) {
    	halt = enabled;
    }
    
    public boolean isHalted() {
    	return halt;
    }
    
    private boolean resizing_enabled = true;
    
    public void setResizing(boolean enabled) {
    	resizing_enabled = enabled;
    }
    
    public boolean isResizingEnabled() {
    	return resizing_enabled;
    }
    
    private void zoom(Point at, int dept) {
		if(halt || !resizing_enabled) {
			return;
		}
		zoomLeapStickToPointOnScreen(dept, at);
		repaint();
    }
    
    private boolean dragging_enabled = true;
    
    public void setDragging(boolean enabled) {
    	dragging_enabled = enabled;
    }
    
    public boolean isDraggingEnabled() {
    	return dragging_enabled;
    }
    
    private Point2D drag_start_offset, drag_start_pos;
    private boolean drag_active = false;
    
    private void startDragging(Point start_position) {
		if(halt || !dragging_enabled) {
			return;
		}
		drag_start_pos = new Point(start_position);
		drag_active = false;
    }
    
    private void dragging(Point at) {
		if(halt || !dragging_enabled) {
			return;
		}
		if(!drag_active) {
			drag_active = true;
			drag_start_offset = new Point2D.Double(offset_x, offset_y);
		}
		offset(drag_start_offset.getX() + (at.x - drag_start_pos.getX())/zoom, drag_start_offset.getY() + (at.y - drag_start_pos.getY())/zoom);
		repaint();
    }
    
    public void move(double x, double y) {
    	offset_x += x;
    	offset_y += y;
    	notifyAllPrespectivePositionListeners();
    }
    
    public void offset(double x, double y) {
    	offset_x = x;
    	offset_y = y;
    	notifyAllPrespectivePositionListeners();
    }
    
    public Point2D getOffset() {
    	return new Point2D.Double(offset_x, offset_y);
    }
    
    public void zoomPivot(double pivot) {
    	PIVOT_ZOOM = pivot;
    }
    
    public void zoom(double z) {
    	zoom = PIVOT_ZOOM * z;
    	if(zoom > PIVOT_ZOOM*MAX_ZOOM) {
    		zoom = PIVOT_ZOOM*MAX_ZOOM;
    	} else if(zoom < PIVOT_ZOOM*MIN_ZOOM) {
    		zoom = PIVOT_ZOOM*MIN_ZOOM;
    	}
    	notifyAllPrespectiveZoomListeners();
    }
    
    public void zoomLeap(int k) {
    	zoom(zoom/PIVOT_ZOOM + k * -LEAP_ZOOM);
    }
    
    public double getZoom() {
    	return zoom;
    }
    
    public void zoomLeapStickToPointOnScreen(int k, Point at) {
    	double prev_zoom = zoom;
    	zoomLeap(k);
    	move(at.getX()*(1/zoom-1/prev_zoom), at.getY()*(1/zoom-1/prev_zoom));
		repaint();
    }
    
    public Point2D translatePoint(Point2D p) {
    	return new Point2D.Double(p.getX()*zoom+offset_x*zoom, p.getY()*zoom+offset_y*zoom);
    }
    
    public Rectangle2D translateRectangle(Rectangle2D d) {
    	Point2D p = translatePoint(new Point2D.Double(d.getX(), d.getY()));
    	return new Rectangle2D.Double(p.getX(), p.getY(), d.getWidth()*zoom, d.getHeight()*zoom);
    }
    
    public Point2D backPoint(Point2D p) {
    	return new Point2D.Double(p.getX()/zoom-offset_x, p.getY()/zoom-offset_y);
    }
    
    public Rectangle2D backRectangle(Rectangle2D d) {
    	Point2D p = backPoint(new Point2D.Double(d.getX(), d.getY()));
    	return new Rectangle2D.Double(p.getX(), p.getY(), d.getWidth()/zoom, d.getHeight()/zoom);
    }
    
    @Override
    public void repaint() {
    	Container parent = getParent();
    	if(parent != null) {
    		parent.repaint();
    	} else {
    		super.repaint();
    	}
    }

    @Override
    protected void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	
    	((Graphics2D)g).scale(zoom, zoom);
    	((Graphics2D)g).translate(offset_x, offset_y);
    }
}
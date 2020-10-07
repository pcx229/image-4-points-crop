package ui;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPanel;


/**
 * show a 4 points selection area on the screen that can be changed by dragging
 * each of the points.
 */
class Area extends JPanel {
	
	// starting position of the dots in percentage relative to container size
	private static final Point2D lt = new Point2D.Float(.1f, .1f), 
								rt = new Point2D.Float(.9f, .1f), 
								rb = new Point2D.Float(.9f, .9f),
								lb = new Point2D.Float(.1f, .9f);
	
	private static final float circles_ratio = .8f;
	private static final int circle_radius = 10;
	
	private static final float grid_line_stroke = 2f, diff_grid_lines_ratio = 1.0f;
	
	private static final int dot_to_mouse_jump_trigger_radius = 30;
	
	// dots arraignment is left-top, right-top, right-bottom, left-bottom
	private Point2D[] dots;
	
	private World2D relative;

	public Area() {
		this(null);
	}
	
	public Area(Point2D[] area) {
		setOpaque(false);
		setLayout(null);
		dots = area;
		// reset area selected when container changed
    	addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {}
			@Override
			public void componentResized(ComponentEvent e) {
				if(dots == null) {
					reset();
				} else {
					repaint();
				}
			}
			@Override
			public void componentMoved(ComponentEvent e) {}
			@Override
			public void componentHidden(ComponentEvent e) {}
		});
    	// drag and drop dots to change area events
		addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				startDragging(e.getPoint());
				if(isHovaringOnAPoint(e.getPoint())) {
					setCursor(new Cursor(Cursor.HAND_CURSOR));
				} else {
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
				dispatchToParent(e);
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				if(dragging(e.getPoint())) {
					setCursor(new Cursor(Cursor.HAND_CURSOR));
					dispatchMoveEventToParent(e);
				} else {
					dispatchToParent(e);
				}
			}

			public void dispatchMoveEventToParent(MouseEvent e) {
				Container parent = getParent();
				if(parent != null) {
					parent.dispatchEvent(new MouseEvent(e.getComponent(), MouseEvent.MOUSE_MOVED, e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e.getButton()));
				}
			}
			
			public void dispatchToParent(MouseEvent e) {
				Container parent = getParent();
				if(parent != null) {
					parent.dispatchEvent(e);
				}
			}
		});
		// dispatch mouse events to parent 
        addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				dispatchToParent(e);
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				dispatchToParent(e);
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				dispatchToParent(e);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				dispatchToParent(e);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				dispatchToParent(e);
			}
			
			public void dispatchToParent(MouseEvent e) {
				Container parent = getParent();
				if(parent != null) {
					parent.dispatchEvent(e);
				}
			}
		});
	}
	
	// changes in area listener
	
	public interface changesInAreaListener {
		void onStart();
		void onProgress(Point2D[] dots, Point2D changed);
		void onEnd();
	}
	
    private List<changesInAreaListener> changes_listeners = new ArrayList<>();
    
    public void addChangesInAreaListener(changesInAreaListener listener) {
    	changes_listeners.add(listener);
    }
    
    public void removeChangesInAreaListener(changesInAreaListener listener) {
    	changes_listeners.remove(listener);
    }
    
    private void notifyAllStartChangesInAreaListeners() {
    	for(changesInAreaListener p : changes_listeners) {
    		p.onStart();
    	}
    }
    
    private void notifyAllProgressChangesInAreaListeners() {
    	for(changesInAreaListener p : changes_listeners) {
    		p.onProgress(dots, dotToDrag);
    	}
    }
    
    private void notifyAllEndChangesInAreaListeners() {
    	for(changesInAreaListener p : changes_listeners) {
    		p.onEnd();
    	}
    }
    
    // hovering
    
    private boolean isHovaringOnAPoint(Point mouse_at) {
    	if(dots == null) {
    		return false;
    	}
    	for(Point2D p : dots) {
			if(relative != null) {
				if(relative.translatePoint(p).distance(mouse_at) < circle_radius) {
					return true;
				}
			} else {
				if(p.distance(mouse_at) < circle_radius) {
					return true;
				}
			}
		}
    	return false;
    }
    
    // dragging
	
    private boolean drag_active = false;
    private Point2D dotToDrag = null;
    
    private void startDragging(Point start_position) {
    	if(drag_active) {
    		notifyAllEndChangesInAreaListeners();
    	}
		drag_active = false;
		dotToDrag = null;
    }
    
    private boolean dragging(Point mouse_at) {
		if(!drag_active) {
			drag_active = true;
    		notifyAllStartChangesInAreaListeners();
			// check which point is close to the cursor
			for(Point2D p : dots) {
				if(relative != null) {
					if(relative.translatePoint(p).distance(mouse_at) < dot_to_mouse_jump_trigger_radius) {
						dotToDrag = p;
					}
				} else {
					if(p.distance(mouse_at) < dot_to_mouse_jump_trigger_radius) {
						dotToDrag = p;
					}
				}
			}
			if(dotToDrag == null) {
				return false;
			}
		}
		if(dotToDrag != null) {
			notifyAllProgressChangesInAreaListeners();
			int x = (int) mouse_at.getX(), y = (int) mouse_at.getY();
			// window constrains
			if(x < 0) {
				x = 0;
			} else if(x > getWidth()) {
				x = getWidth();
			}
			if(y < 0) {
				y = 0;
			} else if(y > getHeight()) {
				y = getHeight();
			}
			// next position 
			Point2D nxt;
			if(relative != null) {
				nxt = relative.backPoint(new Point(x, y));
			} else {
				nxt = new Point(x, y);
			}
			// relative position to other dots constrains
			if(dotToDrag == dots[0]) {
				if(nxt.getX() >= dots[1].getX() || nxt.getY() >= dots[3].getY()) {
					nxt = dotToDrag;
				}
			} else if(dotToDrag == dots[1]) {
				if(nxt.getX() <= dots[0].getX() || nxt.getY() >= dots[2].getY()) {
					nxt = dotToDrag;
				}
			} else if(dotToDrag == dots[2]) {
				if(nxt.getX() <= dots[3].getX() || nxt.getY() <= dots[1].getY()) {
					nxt = dotToDrag;
				}
			} else if(dotToDrag == dots[3]) {
				if(nxt.getX() >= dots[2].getX() || nxt.getY() <= dots[0].getY()) {
					nxt = dotToDrag;
				}
			}
			// set new location
			dotToDrag.setLocation(nxt);
			repaint();
			return true;
		} else {
			return false;
		}
    }
    
    // properties
	
	public void setLocationRelative(World2D relative) {
		this.relative = relative;
		repaint();
	}
	
	public World2D getLocationRelative() {
		return relative;
	}
	
	public void reset() {
		dots = defualtArea();
		repaint();
	}
	
	public Point2D[] getCoords() {
		return dots;
	}
	
	public void setCoords(Point2D[] area) {
		dots = area;
		arrangePointsClockwise();
		repaint();
	}
	
	public void setCoords(int[][] area) {
		dots = new Point2D[]{ new Point2D.Double(area[0][0], area[0][1]),
								new Point2D.Double(area[1][0], area[1][1]),
								new Point2D.Double(area[2][0], area[2][1]),
								new Point2D.Double(area[3][0], area[3][1])};
		arrangePointsClockwise();
		repaint();
	}
	
	private void arrangePointsClockwise() {

		Arrays.sort(dots, new Comparator<Point2D>() {
			@Override
			public int compare(Point2D a, Point2D b) {
				return (int) (a.getY() - b.getY());
			}
		});
		if(dots[0].getX() > dots[1].getX()) {
			Point2D temp = dots[0];
			dots[0] = dots[1];
			dots[1] = temp;
		}
		if(dots[2].getX() < dots[3].getX()) {
			Point2D temp = dots[2];
			dots[2] = dots[3];
			dots[3] = temp;
		}
	}
	
	private Point2D[] defualtArea() {
		
		if(getHeight() == 0 || getWidth() == 0) {
			return null;
		}
		
		Point2D[] x = new Point2D.Double[4];
		x[0] = new Point2D.Double(lt.getX()*getWidth(), lt.getY()*getHeight());
		x[1] = new Point2D.Double(rt.getX()*getWidth(), rt.getY()*getHeight());
		x[2] = new Point2D.Double(rb.getX()*getWidth(), rb.getY()*getHeight());
		x[3] = new Point2D.Double(lb.getX()*getWidth(), lb.getY()*getHeight());
		
    	if(relative != null) {
        	for(int i=0;i<x.length;i++) {
        		x[i] = relative.backPoint(x[i]);
    		}
    	}
    	
    	return x;
	}

    @Override
    protected void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	
    	if(dots == null) {
    		return;
    	}
    	
    	// relative to world
    	Point[] dots_px = new Point[4];
    	if(relative != null) {
    		Point2D temp;
        	for(int i=0;i<dots.length;i++) {
        		temp = relative.translatePoint(dots[i]);
        		dots_px[i] = new Point((int)temp.getX(), (int)temp.getY());
    		}
    	} else {
        	for(int i=0;i<dots.length;i++) {
        		dots_px[i] = new Point((int)dots[i].getX(), (int)dots[i].getY());
    		}
    	}
    	
    	// smooth
		((Graphics2D)g).setRenderingHint(
	            RenderingHints.KEY_ANTIALIASING,
	            RenderingHints.VALUE_ANTIALIAS_ON);

    	// draw grid
    	int[] xPoints = new int[4],
    			yPoints = new int[4];
    	int nPoints = 4;
    	for(int i=0;i<nPoints;i++) {
    		xPoints[i] = (int) (dots_px[i].getX());
    		yPoints[i] = (int) (dots_px[i].getY());
    	}
    	g.setColor(Color.white);
    	((Graphics2D)g).setStroke(new BasicStroke(grid_line_stroke));
    	g.drawPolygon(xPoints, yPoints, nPoints);
    	g.setColor(Color.black);
    	((Graphics2D)g).setStroke(new BasicStroke(grid_line_stroke*diff_grid_lines_ratio));
    	g.drawPolygon(xPoints, yPoints, nPoints);
    	
    	// mesh
    	g.setColor(Color.black);
    	g.drawLine(xPoints[0]-(xPoints[0]-xPoints[1])/3, yPoints[0]-(yPoints[0]-yPoints[1])/3, xPoints[3]-(xPoints[3]-xPoints[2])/3, yPoints[3]-(yPoints[3]-yPoints[2])/3);
    	g.drawLine(xPoints[0]-(xPoints[0]-xPoints[1])/3*2, yPoints[0]-(yPoints[0]-yPoints[1])/3*2, xPoints[3]-(xPoints[3]-xPoints[2])/3*2, yPoints[3]-(yPoints[3]-yPoints[2])/3*2);
    	g.drawLine(xPoints[0]-(xPoints[0]-xPoints[3])/3, yPoints[0]-(yPoints[0]-yPoints[3])/3, xPoints[1]-(xPoints[1]-xPoints[2])/3, yPoints[1]-(yPoints[1]-yPoints[2])/3);
    	g.drawLine(xPoints[0]-(xPoints[0]-xPoints[3])/3*2, yPoints[0]-(yPoints[0]-yPoints[3])/3*2, xPoints[1]-(xPoints[1]-xPoints[2])/3*2, yPoints[1]-(yPoints[1]-yPoints[2])/3*2);
    	
    	// draw circles
		float lw = circle_radius*2, lh = circle_radius*2;
		float sw = lw*circles_ratio, sh = lh*circles_ratio;
    	for(Point p : dots_px) {
    		p.translate(-circle_radius, -circle_radius);
			g.setColor(Color.white);
			g.fillOval((int)p.getX(), (int)p.getY(), (int)lw, (int)lh);
			g.setColor(Color.black);
			g.fillOval((int)(p.getX()+(lw-sw)/2), (int)(p.getY()+(lh-sh)/2), (int)sw, (int)sh);
		}
    }
}

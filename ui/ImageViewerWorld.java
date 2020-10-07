package ui;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * an image viewer that support scaling and dragging.
 */
class ImageViewerWorld extends World2D {

    private BufferedImage image;
    private boolean image_is_loaded;
    
    private String image_not_loaded_msg = "no image is currently loaded";
    
    public ImageViewerWorld(double zoom, double offset_x, double offset_y, BufferedImage image) {
    	super(zoom, offset_x, offset_y);
    	init(image);
    }
    
    public ImageViewerWorld(BufferedImage image) {
    	init(image);
    }
    
    public ImageViewerWorld() {
    	init(null);
    }
    
    private void init(BufferedImage image) {
    	if(image != null) {
            this.image = image;
            image_is_loaded = true;
            fitImageInContainer();
            setHalt(false);
    	} else {
            this.image = null;
            image_is_loaded = false;
            setHalt(true);
    	}
    }
    
    public void setImageNotLoadedMessage(String msg) {
    	image_not_loaded_msg = msg;
    }
    
    public BufferedImage getImage() {
    	return image;
    }
    
    public boolean hasImage() {
    	return image_is_loaded;
    }
    
    public void setImage(BufferedImage image) {
        this.image = image;
        image_is_loaded = true;
        fitImageInContainer();
        setHalt(false);
        repaint();
    }
    
    public void removeImage() {
    	image = null;
    	image_is_loaded = false;
        setHalt(true);
        repaint();
    }
    
    public void setStable(boolean stable) {
    	setResizing(!stable);
    	setDragging(!stable);
    	reset();
    }
    
    @Override
    public void reset() {
    	super.reset();
    	
    	if(image_is_loaded) {
        	fitImageInContainer();
    	}
    	repaint();
    }
    
    private void fitImageInContainer() {
    	Dimension dim_container = new Dimension(getWidth(), getHeight());
    	double width, height;
    	
    	if(dim_container.width == 0 || dim_container.height == 0) {
    		return;
    	}
    	if(!image_is_loaded) {
    		return;
    	}
    	Dimension dim_image = new Dimension(image.getWidth(), image.getHeight());
		// fit image to container
		if (dim_image.width < dim_container.width) {
			if (dim_image.height < dim_container.height) {
				height = dim_image.height;
				width = dim_image.width;
			} else {
				height = dim_container.height;
				width = dim_image.width * height / dim_image.height;
			}
		} else {
			if (dim_image.height < dim_container.height) {
				width = dim_container.width;
				height = dim_image.height * width / dim_image.width;
			} else {
				width = dim_container.width;
				height = dim_image.height * width / dim_image.width;
				if (height > dim_container.height) {
					height = dim_container.height;
					width = dim_image.width * height / dim_image.height;
				}
			}
		}
		// center view
		double z = width/dim_image.width;
		zoomPivot(z);
		zoom(NO_ZOOM);
		width = dim_image.width;
		height = dim_image.height;
		offset((double)dim_container.width/2/zoom - width/2, (double)dim_container.height/2/zoom - height/2);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
    	super.paintComponent(g);

    	// show image is missing message on the screen if there is no image
        if(!image_is_loaded) {
        	g.setFont(g.getFont().deriveFont(15.f));
            FontMetrics metrics = g.getFontMetrics();
        	g.drawString(image_not_loaded_msg, getWidth()/2 - metrics.stringWidth(image_not_loaded_msg)/2, getHeight()/2 - metrics.getHeight()/2);
        	return;
        }
        
        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), this);
    }
}

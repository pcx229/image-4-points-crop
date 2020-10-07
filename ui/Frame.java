package ui;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.*;

import ui.World2D.mousePositionListener;
import util.PerspectiveTransform;

/**
 * shows an editor for cropping an image using 4 points
 */
public class Frame extends JFrame {

	// selected language
	private Locale language;
	
	// language strings
	private ResourceBundle langStrs;
	
	// languages list
	private static final Locale[] supportedLanguages = {
			new Locale("en", "US"), // index 0 used as default
			new Locale("iw", "IL")
	};
	
	// menu components
	private JMenuBar menuBar;
	private JMenu menuFile, 
					menuLanguage, 
					menuView;
	private JMenuItem menuItemOpenFile, 
						menuItemSaveFile, 
						menuItemSaveAsFile, 
						menuItemExit, 
						menuItemEditLabel, 
						menuItemResultLabel;
	private ButtonGroup menuItemlangSelectGroup;
	private JRadioButtonMenuItem[] menuItemlangSelect;
	private JCheckBoxMenuItem menuItemCloserLook, 
								menuItemShowSelectionArea, 
								menuItemStableEditImage, 
								menuItemStableResultImage;
	// actions components
	private JFileChooser imgChooser;
	private FileNameExtensionFilter imgOpenfilter, imgSavefilter;
	
	// image file
	private File image, output;
	private BufferedImage original, cropped;
	
	// image editor
	private PlayGround play;
	
	// options
	private boolean showCloserLook = false,
					showSelectionArea = true,
					editStableImage = false,
					resultStableImage = false;
	
	// image result
	private ImageViewerWorld result;
	
	// tabs
	private JPanel editor, viewer;
	
	// editor
	private JPanel edit_menu;
	private JButton cut, reset_edit;
	private JLabel position;
	
	// viewer
	private JPanel result_menu;
	private JButton back, reset_result;
	
	private void InitComponents() {
		
		// clear 
		getContentPane().removeAll();
		image = output = null;
		original = cropped = null;
		
		// actions components
		imgChooser = new JFileChooser();
		// open accept only gif, jpg, tiff, or png files.
	    imgOpenfilter = new FileNameExtensionFilter(
	            "Just Images .jpg .png .tiff .gif", "jpg", "png", "tiff", "gif");
		// save accept only jpg or png files.
	    imgSavefilter = new FileNameExtensionFilter(
	            "Just As Image .jpg .png", "jpg", "png");
	    imgChooser.setMultiSelectionEnabled(false);
	    imgChooser.setAcceptAllFileFilterUsed(false);

		// create the menu bar
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// file menu
		menuFile = new JMenu(langStrs.getString("menu_file"));
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuFile.getAccessibleContext().setAccessibleDescription(langStrs.getString("menu_file_desc"));
		menuBar.add(menuFile);
		
		menuItemOpenFile = new JMenuItem(langStrs.getString("menu_file_open_file"), KeyEvent.VK_O);
		menuItemOpenFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
		menuItemOpenFile.getAccessibleContext().setAccessibleDescription(langStrs.getString("menu_file_open_file_desc"));
		menuItemOpenFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				open(null);
			}
		});
		menuFile.add(menuItemOpenFile);
		
		menuItemSaveFile = new JMenuItem(langStrs.getString("menu_file_save"), KeyEvent.VK_S);
		menuItemSaveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		menuItemSaveFile.getAccessibleContext().setAccessibleDescription(langStrs.getString("menu_file_save_desc"));
		menuItemSaveFile.setEnabled(false);
		menuItemSaveFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		menuFile.add(menuItemSaveFile);
		
		menuItemSaveAsFile = new JMenuItem(langStrs.getString("menu_file_save_as"), KeyEvent.VK_A);
		menuItemSaveAsFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
		menuItemSaveAsFile.getAccessibleContext().setAccessibleDescription(langStrs.getString("menu_file_save_as_desc"));
		menuItemSaveAsFile.setEnabled(false);
		menuItemSaveAsFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAs();
			}
		});
		menuFile.add(menuItemSaveAsFile);
		
		menuFile.addSeparator();
		
		menuItemExit = new JMenuItem(langStrs.getString("menu_file_exit"), KeyEvent.VK_E);
		menuItemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK));
		menuItemExit.getAccessibleContext().setAccessibleDescription(langStrs.getString("menu_file_exit_desc"));
		menuItemExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		menuFile.add(menuItemExit);
		
		// view menu
		menuView = new JMenu(langStrs.getString("menu_view"));
		menuView.setMnemonic(KeyEvent.VK_V);
		menuView.getAccessibleContext().setAccessibleDescription(langStrs.getString("menu_view_desc"));
		menuBar.add(menuView);
		
		menuItemEditLabel = new JMenuItem(langStrs.getString("menu_view_edit_label"));
		menuItemEditLabel.setEnabled(false);
		menuView.add(menuItemEditLabel);
		
		menuItemCloserLook = new JCheckBoxMenuItem(langStrs.getString("menu_view_closer_look_check_box"));
		menuItemCloserLook.setSelected(showCloserLook);
		menuItemCloserLook.getAccessibleContext().setAccessibleDescription(langStrs.getString("menu_view_closer_look_check_box_desc"));
		menuItemCloserLook.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				editShowCloserLook(menuItemCloserLook.isSelected());
			}
		});
		menuView.add(menuItemCloserLook);

		menuItemShowSelectionArea = new JCheckBoxMenuItem(langStrs.getString("menu_view_selection_area_check_box"));
		menuItemShowSelectionArea.setSelected(showSelectionArea);
		menuItemShowSelectionArea.getAccessibleContext().setAccessibleDescription(langStrs.getString("menu_view_selection_area_check_box_desc"));
		menuItemShowSelectionArea.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				editShowSelectionArea(menuItemShowSelectionArea.isSelected());
			}
		});
		menuView.add(menuItemShowSelectionArea);

		menuItemStableEditImage = new JCheckBoxMenuItem(langStrs.getString("menu_view_stable_image_check_box"));
		menuItemStableEditImage.setSelected(editStableImage);
		menuItemStableEditImage.getAccessibleContext().setAccessibleDescription(langStrs.getString("menu_view_stable_image_check_box_desc"));
		menuItemStableEditImage.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				editStableImage(menuItemStableEditImage.isSelected());
			}
		});
		menuView.add(menuItemStableEditImage);
		
		menuView.addSeparator();
		
		menuItemResultLabel = new JMenuItem(langStrs.getString("menu_view_result_label"));
		menuItemResultLabel.setEnabled(false);
		menuView.add(menuItemResultLabel);

		menuItemStableResultImage = new JCheckBoxMenuItem(langStrs.getString("menu_view_stable_image_check_box"));
		menuItemStableResultImage.setSelected(resultStableImage);
		menuItemStableResultImage.getAccessibleContext().setAccessibleDescription(langStrs.getString("menu_view_stable_image_check_box_desc"));
		menuItemStableResultImage.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resultStableImage(menuItemStableResultImage.isSelected());
			}
		});
		menuView.add(menuItemStableResultImage);

		// language menu
		menuLanguage = new JMenu(langStrs.getString("menu_language"));
		menuLanguage.setMnemonic(KeyEvent.VK_L);
		menuLanguage.getAccessibleContext().setAccessibleDescription(langStrs.getString("menu_language_desc"));
		menuBar.add(menuLanguage);
		
		menuItemlangSelectGroup = new ButtonGroup();
		
		menuItemlangSelect = new JRadioButtonMenuItem[supportedLanguages.length];
		ActionListener onLanguageSelected = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for(int i=0;i<menuItemlangSelect.length;i++) {
					if(menuItemlangSelect[i] == e.getSource()) {
						setLanguage(supportedLanguages[i]); 
					}
				}
			}
		};
		for(int i=0;i<supportedLanguages.length;i++) {
			Locale l = supportedLanguages[i];
			menuItemlangSelect[i] = new JRadioButtonMenuItem(l.getDisplayLanguage());
			if(language == l) {
				menuItemlangSelect[i].setSelected(true);
			}
			menuItemlangSelect[i].addActionListener(onLanguageSelected);
			menuItemlangSelectGroup.add(menuItemlangSelect[i]);
			menuLanguage.add(menuItemlangSelect[i]);
		}

		// editor
		editor = new JPanel();
		editor.setLayout(new BorderLayout());
		add(editor);
		
		// edit image viewer
		play = new PlayGround();
		play.setPreferredSize(new Dimension(500, 500));
		play.setImageNotLoadedMessage(langStrs.getString("editor_image_not_loaded"));
		play.setSelectionAreaVisable(showSelectionArea);
		play.setCloseLookVisable(showCloserLook);
		play.setStable(editStableImage);
		editor.add(play, BorderLayout.CENTER);
		
		// edit tools
		edit_menu = new JPanel();
		edit_menu.setLayout(new BoxLayout(edit_menu, BoxLayout.LINE_AXIS));
		edit_menu.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
		editor.add(edit_menu, BorderLayout.NORTH);
		
		cut = new JButton(langStrs.getString("editor_cut_button"));
		if(image == null) {
			cut.setEnabled(false);
		} else {
			cut.setEnabled(showSelectionArea);
		}
		cut.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				crop(null);
			}
		});
		edit_menu.add(cut);
		
		edit_menu.add(Box.createRigidArea(new Dimension(5,0)));
		
		reset_edit = new JButton(langStrs.getString("editor_reset_button"));
		if(image == null) {
			reset_edit.setEnabled(false);
		} else {
			reset_edit.setEnabled(showSelectionArea);
		}
		reset_edit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				resetEdit();
			}
		});
		edit_menu.add(reset_edit);
		
		edit_menu.add(Box.createHorizontalGlue());
		
		position = new JLabel();
		play.getWorldImageBackground().addMousePositionListener(new mousePositionListener() {
			
			@Override
			public void onChange(Point2D p) {
				if(original != null && p != null) {
					int x = (int)p.getX(), y = (int)p.getY();
					if(x >= 0 && x <= original.getWidth() && y >= 0 && y <= original.getHeight()) {
						position.setText(String.format("(%d, %d)", x, y));
					} else {
						position.setText("");
					}
				} else {
					position.setText("");
				}
			}
		});
		edit_menu.add(position);
		
		// result viewer
		viewer = new JPanel();
		viewer.setLayout(new BorderLayout());
		
		// result image viewer
		result = new ImageViewerWorld();
		result.setPreferredSize(new Dimension(500, 500));
		result.setStable(resultStableImage);
		viewer.add(result, BorderLayout.CENTER);
		
		// result tools
		result_menu = new JPanel();
		result_menu.setLayout(new BoxLayout(result_menu, BoxLayout.LINE_AXIS));
		result_menu.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
		viewer.add(result_menu, BorderLayout.NORTH);
		
		back = new JButton(langStrs.getString("result_back_button"));
		back.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				back();
			}
		});
		result_menu.add(back);
		
		result_menu.add(Box.createRigidArea(new Dimension(5,0)));
		
		reset_result = new JButton(langStrs.getString("result_reset_button"));
		reset_result.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				resetResult();
			}
		});
		result_menu.add(reset_result);
		
		result_menu.add(Box.createRigidArea(new Dimension(5,0)));
	}
	
	public Frame() {
		this(null, null, null);
	}
	
	public Frame(File image) {
		this(image, null, null);
	}
	
	public Frame(File image, int[][] coords) {
		this(image, coords, null);
	}
	
	public Frame(File image, int[][] coords, Locale language) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLanguage(language);
        if(image != null) {
            open(image);
            if(coords != null) {
                play.setCoords(coords);
            }
        }
	}
	
	public void setLanguage(Locale language) {
		
		// remove window
        setVisible(false);
        
		// use default language
		if(language == null) {
			language = supportedLanguages[0];
		}
		this.language = language;
		
		// get language strings
		ResourceBundle.clearCache();
		langStrs = ResourceBundle.getBundle("resources.lang", Frame.this.language);
		
		// put components
		InitComponents();
		updateTitle();
		
		// apply orientation
		applyComponentOrientation(ComponentOrientation.getOrientation(language));
		play.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		viewer.applyComponentOrientation(ComponentOrientation.getOrientation(language));

		// refresh
		pack();
		validate();
		repaint();
		
		// center window
		setLocationRelativeTo(null);
		
		// show window
        setVisible(true);
	}
	
	public void open(File image) {
		if (image == null) {
			// choose an image file to open
		    imgChooser.setFileFilter(imgOpenfilter);
			int returnVal = imgChooser.showOpenDialog(Frame.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				image = imgChooser.getSelectedFile();
			} else {
				return;
			}
		}
		// open the image
		try {
			original = ImageIO.read(image);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(Frame.this, 
					langStrs.getString("error_msg_file_could_not_be_opened_desc"),
					langStrs.getString("error_msg_file_could_not_be_opened_title"), 
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		this.image = image;
		output = null;
		cropped = null;
		
		// change view to result viewer
		if(isAncestorOf(viewer)) {
			remove(viewer);
			add(editor);
		}
		
		// update
		updateTitle();
		play.setImage(original);
		cut.setEnabled(showSelectionArea);
		reset_edit.setEnabled(showSelectionArea);
		menuItemSaveAsFile.setEnabled(false);
		menuItemSaveFile.setEnabled(false);
		
		// refresh
		repaint();
		validate();
	}
	
	public void save() {
		saveCroppedImage();
	}
	
	public void saveAs() {
		// choose a path to save at
	    imgChooser.setFileFilter(imgSavefilter);
	    int returnVal = imgChooser.showSaveDialog(Frame.this);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	 	    output = imgChooser.getSelectedFile();
	    	if(saveCroppedImage()) {
	    		// update
		       menuItemSaveFile.setEnabled(true);
		       updateTitle();
	    	} else {
	    		output = null;
	    	}
	    }
	}
	
	private boolean saveCroppedImage() {
		try {
			// create output file if it doesn't exist
			if(!output.exists()) {
				 output.createNewFile();
			}
			String format = "png"; // default format is png
			if(output.getAbsolutePath().toLowerCase().endsWith(".jpg")) {
				format = "jpg";
			}
			if(format.equals("png") && !output.getAbsolutePath().toLowerCase().endsWith(".png")) {
				output = new File(output.getParent(), output.getName() + ".png");
			}
			// convert ARGB color to RGB for jpg
			BufferedImage writed = cropped;
			if(format.equals("jpg")) {
				writed = new BufferedImage(cropped.getWidth(), cropped.getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics g = writed.getGraphics();
				g.drawImage(cropped, 0, 0, null);
				g.dispose();
			}
			// save image
	        ImageIO.write(writed, format, output);
		} catch(IOException e) {
			JOptionPane.showMessageDialog(Frame.this, 
					langStrs.getString("error_msg_file_could_not_be_saved_desc"),
					langStrs.getString("error_msg_file_could_not_be_saved_title"), 
					JOptionPane.ERROR_MESSAGE);
        	return false;
		}
        return true;
	}
	
	public void exit() {
		System.exit(0);
	}
	
	public void editShowCloserLook(boolean active) {
		showCloserLook = active;
		play.setCloseLookVisable(showCloserLook);
	}
	
	public void editStableImage(boolean active) {
		editStableImage = active;
		play.setStable(editStableImage);
	}
	
	public void editShowSelectionArea(boolean active) {
		showSelectionArea = active;
		play.setSelectionAreaVisable(showSelectionArea);
		cut.setEnabled(showSelectionArea && (image != null));
		reset_edit.setEnabled(showSelectionArea && (image != null));
	}
	
	public void resultStableImage(boolean active) {
		resultStableImage = active;
		result.setStable(resultStableImage);
	}
	
	public void crop(int[][] area) {
		if(area == null) {
			Point2D[] p = play.getCoords();
			area = new int[][] { {(int) p[0].getX(), (int) p[0].getY()},
					{(int) p[1].getX(), (int) p[1].getY()},
					{(int) p[2].getX(), (int) p[2].getY()},
					{(int) p[3].getX(), (int) p[3].getY()}};
		}
		try {
			// Crop Image
			
			// arrange points to left-top right-top right-bottom left-bottom 
			PerspectiveTransform.arrange4PointsClockwise(area);
			
			// calculate size for cropped image
			int[][] max = PerspectiveTransform.maximizeSquareTranslation(area);
			int width = max[2][0], height = max[2][1];
			
			// find perspective transform matrix
			double[][] mmt = PerspectiveTransform.matrix(area, max);

			// original image
			BufferedImage source = play.getWorldImageBackground().getImage();
			
			// build cropped image
			cropped = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			PerspectiveTransform.writeTransformed(source, width, height, mmt, cropped);
			
			// setup cropped image viewer
			result.setImage(cropped);
			
			// change view to result viewer
			remove(editor);
			add(viewer);
			
			// menu changes
			menuItemSaveAsFile.setEnabled(true);
			if(output != null) {
				menuItemSaveFile.setEnabled(true);
			}
			
			// refresh
			validate();
			repaint();
			
		} catch(Exception e) {
			JOptionPane.showMessageDialog(Frame.this, 
					langStrs.getString("error_msg_file_area_could_not_be_cropped_desc"),
					langStrs.getString("error_msg_file_area_could_not_be_cropped_title"), 
					JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
	
	public void back() {
		
		// change view to editor viewer
		remove(viewer);
		add(editor);
		
		// menu changes
		menuItemSaveAsFile.setEnabled(false);
		menuItemSaveFile.setEnabled(false);
		
		// refresh
		validate();
		repaint();
	}
	
	public void resetEdit() {
		play.reset();
	}
	
	public void resetResult() {
		result.reset();
	}
	
	private void updateTitle() {
		if(image == null) {
			setTitle(langStrs.getString("title"));
		} else {
			if(output != null) {
				setTitle(String.format(langStrs.getString("title_with_file_opened_and_save_at"), image.getAbsolutePath(), output.getAbsolutePath()));
			} else {
				setTitle(String.format(langStrs.getString("title_with_file_opened"), image.getAbsolutePath()));
			}
		}
	}
	
	public static void showWindow(final File image, final int[][] coords, final Locale language) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Frame(image, coords, language);
			}
		});
	}
}

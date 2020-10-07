package cli;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import util.PerspectiveTransform;

/**
 * command line tool for cropping an image using four points, adjust the view perspective
 * of the cropped image.
 * 
 * usage: java -jar bin [options] -c <list of four (x,y) coordinates> -i <file>
 * 
 * crop an image using four points with adjusted perspective.
 * if output path was not provided the result image will be stored in the folder of the input image
 * as [input image name]-4crop.[format]
 * note: the image orientation cannot be identified, your image may be rotated
 * even if it viewed normal, in this case the result will be corrupted, try to save it with the current
 * orientation to a new file.
 * 
 *  -c,--coords <list of four (x,y) coordinates>   coordinates that represent a rectangular shape,
 *                                                 arraignment is not relevant.
 *                                                 example: [(3,3),(16,56),(73,55),(62,14)]
 *                                                 note: if list contain spaces it should be
 *                                                 encapsulated with parentheses
 *  -f,--output-format <format=png>                choose output format jpg or png(default)
 *  -g,--gui                                       shows a graphical interface that the user can
 *                                                 interact with
 *  -h,--help                                      print this message
 *  -i,--image <file>                              input image path
 *  -o,--output-image <file>                       output image path
 * 
 */
public class ImageFourPointsCrop {
	
	public static void main(String[] args) {

		// Command Line Arguments
		
	    // options
		Option coordsOption = 
				Option.builder("c")
		                .longOpt("coords")
		                .desc("coordinates that represent a rectangular shape, " + 
		                	  "arraignment is not relevant.\n" +
		                	  "example: [(3,3),(16,56),(73,55),(62,14)]\n" +
		                	  "note: if list contain spaces it should be encapsulated with parentheses")
						.argName("list of four (x,y) coordinates")
		                .hasArg()
		                .build();
		Option imageInputfileOption = 
				Option.builder("i")
		                .longOpt("image")
		                .desc("input image path")
						.argName("file")
		                .hasArg()
		                .build();
		Option imageOutputFileOption = 
				Option.builder("o")
		                .longOpt("output-image")
		                .desc("output image path")
						.argName("file")
		                .hasArg()
		                .build();
		Option imageOutputFormatOption = 
				Option.builder("f")
						.longOpt("output-format")
		                .desc("choose output format jpg or png(default)")
		                .hasArg()
		                .argName("format=png")
		                .build();
		Option helpOption = 
				Option.builder("h")
						.longOpt("help")
		                .desc("print this message")
		                .hasArg(false)
		                .build();
		Option guiOption = 
				Option.builder("g")
						.longOpt("gui")
		                .desc("shows a graphical interface that the user can interact with")
		                .hasArg(false)
		                .build();

		Options options = new Options();
		options.addOption(coordsOption);
		options.addOption(imageInputfileOption);
		options.addOption(imageOutputFileOption);
		options.addOption(imageOutputFormatOption);
		options.addOption(helpOption);
		options.addOption(guiOption);
		
		// parse
		File imgFile = null;
		int[][] rectangle = null;
		File output = null;
		String output_format = "png";
		boolean show_gui = false;
		
	    CommandLineParser parser = new DefaultParser();
	    try {
	        CommandLine line = parser.parse(options, args);
			// help
	        if(line.hasOption("help")) {
	        	HelpFormatter formatter = new HelpFormatter();
	    		formatter.setWidth(100);
	    		String pname = new File(ImageFourPointsCrop.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
	    		formatter.printHelp("java -jar " + pname + " [options] -c <list of four (x,y) coordinates> -i <file>", 
	    							"\ncrop an image using four points with adjusted perspective.\n" + 
	    						    "if output path was not provided the result image will" +
	    							" be stored in the folder of the input image\nas [input image name]-4crop.[format]\n" +
	    							"note: the image orientation cannot be identified, your image may be rotated\n" +
	    							"even if it viewed normal, in this case the result will be corrupted, try to save it with the current orientation to a new file.\n\n", options, null, false);
	        	System.exit(0);
	        }
	        // gui
	        if(line.hasOption("gui")) {
	        	show_gui = true;
	        }
	        // output file
	        if(line.hasOption("output-image")) {
	        	output = new File(line.getOptionValue("output-image"));
	        }
	        // output format
	        if(line.hasOption("output-format")) {
	        	output_format = line.getOptionValue("output-format").toLowerCase();
	        	if(!output_format.equals("png") && !output_format.equals("jpg")) {
	        		System.err.println("error: output format is not recoginzed, sould be png or jpg.");
		    	    System.exit(0);
	        	}
	        }
	        // image file
	        if(line.hasOption("image")) {
		        if(!(imgFile=new File(line.getOptionValue("image"))).exists()) {
		        	System.err.println("error: input image file dose not exist");
		    	    System.exit(0);
		        }
	        } else {
	        	if(!show_gui) {
	        		System.err.println("error: input image file is missing");
	        		System.exit(0);
	        	}
	        }
	        // coordinates
	        if(line.hasOption("coords")) {
		        String coords = line.getOptionValue("coords");
		        Matcher m1 = Pattern.compile("\\((\\d+,[ ]*\\d+)\\)").matcher(coords);
				ArrayList<int[]> temp = new ArrayList<>();
				while(m1.find()) {
					Matcher m2 = Pattern.compile("\\d+").matcher(m1.group(1));
					int x, y;
					m2.find();
					x = Integer.parseInt(m2.group(0));
					m2.find();
					y = Integer.parseInt(m2.group(0));
					temp.add(new int[]{ x, y });
				}
				if(temp.size() != 4) {
					System.err.println("error: coordinates are missing, 4 points are required.");
					System.exit(0);
				}
				rectangle = temp.toArray(new int[4][2]);
	        } else {
	        	if(!show_gui) {
		        	System.err.println("error: coordinates are missing");
		    	    System.exit(0);
	        	}
	        }
	    }
	    catch(ParseException exp) {
	        System.err.println("error: " + exp.getMessage());
		    System.exit(0);
	    }
	    
	    // UI
	    
	    if(show_gui) {
	    	ui.Frame.showWindow(imgFile, rectangle, null);
	    	return;
	    }
		
		// Prepare Data

		// get the original image
		BufferedImage image = null;
		try {
			image = ImageIO.read(imgFile);
		} catch(IOException e) {
        	System.err.println("error: invalid input image");
    	    System.exit(0);
		}

		// arrange points to left-top right-top right-bottom left-bottom 
		PerspectiveTransform.arrange4PointsClockwise(rectangle);
		
		// calculate size for cropped image
		int[][] max = PerspectiveTransform.maximizeSquareTranslation(rectangle);
		int width = max[2][0], height = max[2][1];
		
		// find perspective transform matrix
		double[][] mmt = PerspectiveTransform.matrix(rectangle, max);
		
		// Construct Cropped Image
        
		System.out.print("Building...");
		
        // build cropped image
		BufferedImage cropped = null;
		if(output_format.equals("png")) {
			cropped = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		} else if(output_format.equals("jpg")) {
			cropped = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}
        PerspectiveTransform.writeTransformed(image, width, height, mmt, cropped);
        
		System.out.println("done!");
 
        // Save As...
        
		if(output == null) {
			String path = imgFile.getAbsoluteFile().getParent() + "/" + imgFile.getName().replaceAll("\\..+$", "").concat("-4crop" + "." + output_format);
			output = new File(path);
		}
		try {
			if(!output.exists()) {
				 output.createNewFile();
			}
	        ImageIO.write(cropped, output_format, output);
		} catch(IOException e) {
        	System.err.println("error: failed to save image, path may be problematic try saving to a different directory " + 
        							"that dose not contains spaces or Unicode Letters in the its path.");
    	    System.exit(0);
		}
        
        System.out.println("Saved to " + output.getPath());
        
	}
}
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.util.MathArrays;

/**
 * command line tool for cropping an image using four points, adjust the view perspective
 * of the cropped image.
 * 
 * usage: java -jar bin.jar [options] -c <list of four (x,y) coordinates> -i <file>
 * crop an image using four points and adjust view perspective.
 * if output path was not provided the result image will be stored in the folder of the input image 
 * as [input image name]-4crop.[format]
 *  -c,--coords <list of four (x,y) coordinates>   coordinates that represent a rectangular shape,
 *                                                 arraignment is not relevant.
 *                                                 example: [(3,3),(16,56),(73,55),(62,14)]
 *                                                 note: if list contain spaces it should be
 *                                                 encapsulated with parentheses
 *  -h,--help                                      print this message
 *  -i,--image <file>                              input image path
 *  -o,--output-image <file>                       output image path
 *     --output-format <format=png>                choose output format jpg or png(default)
 */
public class ImageFourPointsCrop {
	
	public static void main(String[] args) {

		// Command Line Arguments

		final String OPTION_IMAGE = "image",
					 OPTION_COORDINATES = "coords",
					 OPTION_OUTPUT_IMAGE = "output-image",
					 OPTION_OUTPUT_IMAGE_FORMAT = "output-format",
					 OPTION_HELP = "help";
		
	    // options
		Option coordsOption = 
				Option.builder("c")
		                .longOpt(OPTION_COORDINATES)
		                .desc("coordinates that represent a rectangular shape, " + 
		                	  "arraignment is not relevant.\n" +
		                	  "example: [(3,3),(16,56),(73,55),(62,14)]\n" +
		                	  "note: if list contain spaces it should be encapsulated with parentheses")
						.argName("list of four (x,y) coordinates")
		                .hasArg()
		                .build();
		Option imageInputfileOption = 
				Option.builder("i")
		                .longOpt(OPTION_IMAGE)
		                .desc("input image path")
						.argName("file")
		                .hasArg()
		                .build();
		Option imageOutputFileOption = 
				Option.builder("o")
		                .longOpt(OPTION_OUTPUT_IMAGE)
		                .desc("output image path")
						.argName("file")
		                .hasArg()
		                .build();
		Option imageOutputFormatOption = 
				Option.builder()
						.longOpt(OPTION_OUTPUT_IMAGE_FORMAT)
		                .desc("choose output format jpg or png(default)")
		                .hasArg()
		                .argName("format=png")
		                .build();
		Option helpOption = 
				Option.builder("h")
						.longOpt(OPTION_HELP)
		                .desc("print this message")
		                .hasArg(false)
		                .build();

		Options options = new Options();
		options.addOption(coordsOption);
		options.addOption(imageInputfileOption);
		options.addOption(imageOutputFileOption);
		options.addOption(imageOutputFormatOption);
		options.addOption(helpOption);
		
		// parse
		File imgFile = null;
		ArrayList<double[]> rectangle = new ArrayList<double[]>();
		File output = null;
		String output_format = "png";
		
	    CommandLineParser parser = new DefaultParser();
	    try {
	        CommandLine line = parser.parse(options, args);
			// help
	        if(line.hasOption(OPTION_HELP)) {
	        	HelpFormatter formatter = new HelpFormatter();
	    		formatter.setWidth(100);
	    		String pname = new File(ImageFourPointsCrop.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
	    		formatter.printHelp("java -jar " + pname + " [options] -c <list of four (x,y) coordinates> -i <file>", 
	    							"crop an image using four points with adjusted perspective.\n" + 
	    						    "if output path was not provided the result image will" +
	    							" be stored in the folder of the input image\nas [input image name]-4crop.[format]", options, null, false);
	        	System.exit(0);
	        }
	        // output file
	        if(line.hasOption(OPTION_OUTPUT_IMAGE)) {
	        	output = new File(line.getOptionValue(OPTION_OUTPUT_IMAGE));
	        }
	        // output format
	        if(line.hasOption(OPTION_OUTPUT_IMAGE_FORMAT)) {
	        	output_format = line.getOptionValue(OPTION_OUTPUT_IMAGE_FORMAT).toLowerCase();
	        	if(!output_format.equals("png") && !output_format.equals("jpg")) {
	        		System.err.println("error: output format is not recoginzed, sould be png or jpg.");
		    	    System.exit(0);
	        	}
	        }
	        // image file
	        if(line.hasOption(OPTION_IMAGE)) {
		        if(!(imgFile=new File(line.getOptionValue(OPTION_IMAGE))).exists()) {
		        	System.err.println("error: input image file dose not exist");
		    	    System.exit(0);
		        }
	        } else {
	        	System.err.println("error: input image file is missing");
	    	    System.exit(0);
	        }
	        // coordinates
	        if(line.hasOption(OPTION_COORDINATES)) {
		        String coords = line.getOptionValue(OPTION_COORDINATES);
		        Matcher m1 = Pattern.compile("\\((\\d+,[ ]*\\d+)\\)").matcher(coords);
				while(m1.find()) {
					Matcher m2 = Pattern.compile("\\d+").matcher(m1.group(1));
					int x, y;
					m2.find();
					x = Integer.parseInt(m2.group(0));
					m2.find();
					y = Integer.parseInt(m2.group(0));
					rectangle.add(new double[]{ x, y });
				}
				if(rectangle.size() != 4) {
					System.err.println("error: coordinates are missing, 4 points are required.");
					System.exit(0);
				}
	        } else {
	        	System.err.println("error: coordinates are missing");
	    	    System.exit(0);
	        }
	    }
	    catch(ParseException exp) {
	        System.err.println("error: " + exp.getMessage());
		    System.exit(0);
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
		WritableRaster org_roster = image.getRaster();
		
		// arrange points to left-top left-bottom right-top right-bottom
		rectangle.sort(new Comparator<double[]>() {

			@Override
			public int compare(double[] a, double[] b) {
				if(a[0] != b[0]) {
					return (int) (a[0] - b[0]);
				}
				return (int) (b[1] - a[1]);
			}
			
		});
		
		// calculate size for cropped image
		int width, height;
		width = (int) Math.max(MathArrays.distance(rectangle.get(0), rectangle.get(2)), MathArrays.distance(rectangle.get(1), rectangle.get(3)));
		height = (int) Math.max(MathArrays.distance(rectangle.get(0), rectangle.get(1)), MathArrays.distance(rectangle.get(2), rectangle.get(3)));
		
		System.out.println("Cropped image size " + width + "x" + height + "px");

		// calculate multiplication matrix
		double[][] rc1 = { {0, 0},		{width, 0}, 
					       {0, height}, {width, height} };
		double[][] rc2 = { rectangle.get(1), rectangle.get(3), 
						   rectangle.get(0), rectangle.get(2) };
		
		double[][] A = { { rc1[0][0], rc1[0][1], 1, 0, 0, 0, -rc2[0][0]*rc1[0][0], -rc2[0][0]*rc1[0][1], -rc2[0][0] },
						 { 0, 0, 0, rc1[0][0], rc1[0][1], 1, -rc2[0][1]*rc1[0][0], -rc2[0][1]*rc1[0][1], -rc2[0][1] },
						 { rc1[1][0], rc1[1][1], 1, 0, 0, 0, -rc2[1][0]*rc1[1][0], -rc2[1][0]*rc1[1][1], -rc2[1][0] },
						 { 0, 0, 0, rc1[1][0], rc1[1][1], 1, -rc2[1][1]*rc1[1][0], -rc2[1][1]*rc1[1][1], -rc2[1][1] },
						 { rc1[2][0], rc1[2][1], 1, 0, 0, 0, -rc2[2][0]*rc1[2][0], -rc2[2][0]*rc1[2][1], -rc2[2][0] },
						 { 0, 0, 0, rc1[2][0], rc1[2][1], 1, -rc2[2][1]*rc1[2][0], -rc2[2][1]*rc1[2][1], -rc2[2][1] },
						 { rc1[3][0], rc1[3][1], 1, 0, 0, 0, -rc2[3][0]*rc1[3][0], -rc2[3][0]*rc1[3][1], -rc2[3][0] },
						 { 0, 0, 0, rc1[3][0], rc1[3][1], 1, -rc2[3][1]*rc1[3][0], -rc2[3][1]*rc1[3][1], -rc2[3][1] },
				 
						 { 0, 0, 0, 0, 0, 0, 0, 0, 1 } };
		
		double[] B = { 0, 0, 0, 0, 0, 0, 0, 0, 1 };
		
		RealMatrix x = null;
		try {
			x = new LUDecomposition(new Array2DRowRealMatrix(A))
								.getSolver()
								.solve(new Array2DRowRealMatrix(B));
		} catch(NonSquareMatrixException | SingularMatrixException e) {
        	System.err.println("error: coordinates are not a valid shape or transformation is not possible");
    	    System.exit(0);
		}
		
		double[][] X = x.getData();
		
		double[][] mmt = new double[][]{{ X[0][0], X[1][0], X[2][0] }, 
										{ X[3][0], X[4][0], X[5][0] }, 
										{ X[6][0], X[7][0], X[8][0] }};

		// Construct Cropped Image
        
		System.out.print("Building...");
		
        // build cropped image pixels
        BufferedImage cropped = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        WritableRaster cp_roster = cropped.getRaster();
        double PX, PY, PW;
    	double[][] temp = new double[4][4];
		double[] pixel = new double[4];
        for(int px=0;px<width;px++) {
            for(int py=0;py<height;py++) {
            	PW = px*mmt[2][0] + py*mmt[2][1] + mmt[2][2];
            	PX = (px*mmt[0][0] + py*mmt[0][1] + mmt[0][2]) / PW;
            	PY = (px*mmt[1][0] + py*mmt[1][1] + mmt[1][2]) / PW;
            	
            	// average pixel value relative to its floating point position
        		double dx = PX - (int)PX, dy = PY - (int)PY;
        		double tl = (1-dx)*(1-dy), tr = dx*(1-dy), bl = (1-dx)*dy, br = dx*dy;
        		org_roster.getPixel((int)PX, (int)PY, temp[0]);
        		org_roster.getPixel((int)PX+1, (int)PY, temp[1]);
        		org_roster.getPixel((int)PX, (int)PY+1, temp[2]);
        		org_roster.getPixel((int)PX+1, (int)PY+1, temp[3]);
        		for(int i=0;i<4;i++) {
        			pixel[i] = temp[0][i]*tl + temp[1][i]*tr + temp[2][i]*bl + temp[3][i]*br;
        		}
            	cp_roster.setPixel(px, py, pixel);
            }
        }
		
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
        	System.err.println("error: failed to save image");
    	    System.exit(0);
		}
        
        System.out.println("Saved to " + output.getPath());
        
	}
}
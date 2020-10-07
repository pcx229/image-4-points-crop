package util;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;


public class Pixels {

	/**
	 * get the pixel value relative to its floating point position. <br>
	 * try to predict what will be the pixel value for a floating point position
	 * by proportionally averaging the four pixels near by.
	 * <pre>
	 * for example:
	 * given (43.23, 10.2)
	 * looking at four closest values where the wanted pixel somewhere in between
	 * +-------+-------+
	 * |(43,10)|(44,10)|
	 * +-------+-------+
	 * |(43,11)|(44,11)|
	 * +-------+-------+
	 * the final value will be calculated as
	 * (0.77*0.8) * (43, 10) + (0.23*0.8) * (44, 10) + (0.77*0.2) * (43, 11) + (0.23*0.2) * (44, 11) 
	 * </pre>
	 * @param x the x floating point value
	 * @param y the y floating point value
	 * @param image an image
	 * @returns calculated pixel value
	 */
	public static int smoth(double x, double y, BufferedImage image) {
		double dx = Math.abs(x) -  Math.abs((int)x), dy = Math.abs(y) -  Math.abs((int)y);
		double tl = (1-dx)*(1-dy), tr = dx*(1-dy), bl = (1-dx)*dy, br = dx*dy;
		int X = (int)x, Y = (int)y;
		
		int p11, p12, p21, p22;
		p11 = p12 = p21 = p22 = 0;
		if(X >= 0 && X < image.getWidth() && Y >= 0 && Y < image.getHeight()) {
			p11 = image.getRGB(X, Y);
			if(X+1 < image.getWidth()) {
				p12 = image.getRGB(X+1, Y);
			}
			if(Y+1 < image.getHeight()) {
				p21 = image.getRGB(X, Y+1);
			}
			if(X+1 < image.getWidth() && Y+1 < image.getHeight()) {
				p22 = image.getRGB(X+1, Y+1);
			}
		} else {
			return 0;
		}
		
		int blue = (int) ((p11 & 0xFF)*tl + (p12 & 0xFF)*tr + (p21 & 0xFF)*bl + (p22 & 0xFF)*br),
			green = (int) (((p11>>8) & 0xFF)*tl + ((p12>>8) & 0xFF)*tr + ((p21>>8) & 0xFF)*bl + ((p22>>8) & 0xFF)*br),
			red = (int) (((p11>>16) & 0xFF)*tl + ((p12>>16) & 0xFF)*tr + ((p21>>16) & 0xFF)*bl + ((p22>>16) & 0xFF)*br),
			alpha = (int) (((p11>>>24) & 0xFF)*tl + ((p12>>>24) & 0xFF)*tr + ((p21>>>24) & 0xFF)*bl + ((p22>>>24) & 0xFF)*br);

		int result = ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
		
		return result;
	}
}

package util;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.NonSquareMatrixException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.util.MathArrays;


public class PerspectiveTransform {

	/**
	 * find a perspective transform matrix to a given two rectangular shapes.
	 * <pre>
	 * the matrix is the solution to the following:
	 * source rectangular shape is (Xi, Yi) and the destination is (xi, yi)
	 * +-                                     -+
	 * | x1 y1 1 0  0  0 -X1*x1 -X1*y1 -X1 | 0 | x11
	 * | 0  0  0 x1 y1 1 -Y1*x1 -Y1*y1 -Y1 | 0 | x12
	 * | x2 y2 1 0  0  0 -X2*x2 -X2*y2 -X2 | 0 | x13
	 * | 0  0  0 x2 y2 1 -Y2*x2 -Y2*y2 -Y2 | 0 | x21
	 * | x3 y3 1 0  0  0 -X3*x3 -X3*y3 -X3 | 0 | x22
	 * | 0  0  0 x3 y3 1 -Y3*x3 -Y3*y3 -Y3 | 0 | x23
	 * | x4 y4 1 0  0  0 -X4*x4 -X4*y4 -X4 | 0 | x31
	 * | 0  0  0 x4 y4 1 -Y4*x4 -Y4*y4 -Y4 | 0 | x32
	 * | 0  0  0 0  0  0  0      0      1  | 1 | x33
	 * +-                                     -+
	 * specifying that x33 is 1 since there is more then one solution(any non zero number will suffices).
	 * the result matrix is:
	 * +-           -+
	 * | x11 x12 x13 |
	 * | x21 x22 x23 |
	 * | x31 x32 x33 |
	 * +-           -+
	 * </pre>
	 * @param src a rectangular shape represented by an array of four (x, y) coordinates 
	 * @param dest a rectangular shape represented by an array of four (x, y) coordinates that corresponds to the src coordinates
	 * @return the multiplication matrix that can turn the source rectangular to the destination rectangular.
	 * @throws DegenerateMatrixException if the matrix is degenerate meaning there is not a single solution to the problem.
	 */
	public static double[][] matrix(int[][] src, int[][] dest) 
			throws NonSquareMatrixException, SingularMatrixException {
		
		double[][] A = 
			   { { dest[0][0], dest[0][1], 1, 0, 0, 0, -src[0][0]*dest[0][0], -src[0][0]*dest[0][1], -src[0][0] },
				 { 0, 0, 0, dest[0][0], dest[0][1], 1, -src[0][1]*dest[0][0], -src[0][1]*dest[0][1], -src[0][1] },
				 { dest[1][0], dest[1][1], 1, 0, 0, 0, -src[1][0]*dest[1][0], -src[1][0]*dest[1][1], -src[1][0] },
				 { 0, 0, 0, dest[1][0], dest[1][1], 1, -src[1][1]*dest[1][0], -src[1][1]*dest[1][1], -src[1][1] },
				 { dest[2][0], dest[2][1], 1, 0, 0, 0, -src[2][0]*dest[2][0], -src[2][0]*dest[2][1], -src[2][0] },
				 { 0, 0, 0, dest[2][0], dest[2][1], 1, -src[2][1]*dest[2][0], -src[2][1]*dest[2][1], -src[2][1] },
				 { dest[3][0], dest[3][1], 1, 0, 0, 0, -src[3][0]*dest[3][0], -src[3][0]*dest[3][1], -src[3][0] },
				 { 0, 0, 0, dest[3][0], dest[3][1], 1, -src[3][1]*dest[3][0], -src[3][1]*dest[3][1], -src[3][1] },
				 { 0, 0, 0, 0, 0, 0, 0, 0, 1 } };

		double[] b = { 0, 0, 0, 0, 0, 0, 0, 0, 1 };
		
		RealMatrix x = new LUDecomposition(new Array2DRowRealMatrix(A))
								.getSolver()
								.solve(new Array2DRowRealMatrix(b));
		
		double[][] X = x.getData();
		
		double[][] mmt = new double[][]{{ X[0][0], X[1][0], X[2][0] }, 
										{ X[3][0], X[4][0], X[5][0] }, 
										{ X[6][0], X[7][0], X[8][0] }};

		return mmt;
	}
	
	/**
	 * write an image to destination from the source image with the 
	 * perspective transform matrix and dimensions specified.
	 * <pre>
	 * perspective transform matrix:
	 * +-           -+
	 * | x11 x12 x13 |
	 * | x21 x22 x23 |
	 * | x31 x32 x33 |
	 * +-           -+
	 * every pixel will be transformed as followed: 
	 * w = source(x)*x31 + source(y)*x32 + x33
	 * destination(x) = source(x)*x11 + source(y)*x12 + x13)/w
	 * destination(y) = source(x)*x21 + source(y)*x22 + x23)/w
	 * </pre>
	 * @param source the original image
	 * @param width the width of the destination image
	 * @param height the height of the destination image
	 * @param mmt a perspective transform matrix made by {@link #matrix(int[][],int[][]) matrix}
	 * @param destination the destination image to be built by this method
	 */
	public static void writeTransformed(BufferedImage source, int width, int height, double[][] mmt, BufferedImage destination) {
		
        double X, Y, W;
    	
        for(int x=0;x<width;x++) {
            for(int y=0;y<height;y++) {
            	W = x*mmt[2][0] + y*mmt[2][1] + mmt[2][2];
            	X = (x*mmt[0][0] + y*mmt[0][1] + mmt[0][2]) / W;
            	Y = (x*mmt[1][0] + y*mmt[1][1] + mmt[1][2]) / W;
            	destination.setRGB(x, y, Pixels.smoth(X, Y, source));
            }
        }
	}

	/** 
	 * arrange 4 points clockwise:<br>
	 *	 left-top(->0, ->0) right-top right-bottom left-bottom
	 * <pre>
	 *	example:
	 *  for -		A(227, 641) B(810, 770) C(800,200)  D(90, 67)
	 *  result -	D(90, 67)   C(800,200)  B(810, 770) A(227, 641) 
	 * </pre>
	 * @param points an array of 4 points (x, y) to arrange, the changes 
	 * 			will be made on the original array.
	 */
	public static void arrange4PointsClockwise(int[][] points) {

		Arrays.sort(points, new Comparator<int[]>() {
			@Override
			public int compare(int[] a, int[] b) {
				return a[1] - b[1];
			}
		});
		if(points[0][0] > points[1][0]) {
			int[] temp = points[0];
			points[0] = points[1];
			points[1] = temp;
		}
		if(points[2][0] < points[3][0]) {
			int[] temp = points[2];
			points[2] = points[3];
			points[3] = temp;
		}
	}
	
	/**
	 * get a maximized square of the given rectangle<br>
	 * <pre>
	 * example:
	 * four points arranged clockwise: 
	 * A(90, 67) B(800,200) C(810, 770) D(227, 641) 
	 * will translate to:
	 * distance(A, B) > distance(C, D) -> width = distance(C, D) = 722
	 * distance(A, D) > distance(B, C) -> height = distance(B, C) = 590
	 * A(0, 0) B(width, 0) C(width, height) D(0, height)
	 * A(0, 0) B(722, 0)   C(722, 590)      D(0, 590) 
	 * </pre>
	 * @param points 4 points representing the rectangle ordered clockwise, see {@link #arrange4PointsClockwise(int[][])}
	 * @return
	 */
	public static int[][] maximizeSquareTranslation(int[][] points) {
		int width, height;
		width = (int) Math.max(MathArrays.distance(points[0], points[1]), MathArrays.distance(points[2], points[3]));
		height = (int) Math.max(MathArrays.distance(points[0], points[3]), MathArrays.distance(points[1], points[2]));
		return new int[][]{{0, 0}, {width, 0}, 
							{width, height}, {0, height}};
	}
}

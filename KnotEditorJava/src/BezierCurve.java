import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;


public class BezierCurve {

	private static final double EPSILON = 0.00000000001;

	//initial control points
	public final double x1, y1, cx1, cy1, cx2, cy2, x2, y2;

	//coefficients of parametric form
	private final double a3, a2, a1, a0, b3, b2, b1, b0;

	//coefficients of implicit form
	private final double uxxx, uxxy, uxyy, uyyy, uxx, uxy, uyy, ux, uy, u0;

	/*
	 * Create a new cubic Bezier curve from control points
	 * 
	 * See http://www.mare.ee/indrek/misc/2d.pdf for details on math
	 */
	public BezierCurve(double x1, double y1, double cx1, double cy1, double cx2, double cy2, double x2, double y2) {
		//Calculate the parametric form of the Bezier curve
		a3 = -x1 + 3.0*cx1 - 3.0*cx2 + x2;
		a2 = 3.0*x1 - 6.0*cx1 + 3.0*cx2;
		a1 = -3.0*x1 + 3.0*cx1;
		a0 = x1;
		b3 = -y1 + 3.0*cy1 - 3.0*cy2 + y2;
		b2 = 3.0*y1 - 6.0*cy1 + 3.0*cy2;
		b1 = -3.0*y1 + 3.0*cy1;
		b0 = y1;

		System.out.printf("x(t) = %gt^3 + %gt^2 + %gt + %g\n", a3, a2, a1, a0);
		System.out.printf("y(t) = %gt^3 + %gt^2 + %gt + %g\n", b3, b2, b1, b0);

		//And also find its implicit form
		uxxx = b3*b3*b3;
		uxxy = -3.0*a3*b3*b3;
		uxyy = 3.0*b3*a3*a3;
		uyyy = -a3*a3*a3;
		uxx = -3.0*a3*b1*b2*b3 + a1*b2*b3*b3 - a2*b3*b2*b2 + 2.0*a2*b1*b3*b3 + 3.0*a3*b0*b3*b3 + a3*b2*b2*b2 - 3.0*a0*b3*b3*b3;
		uxy = a1*a3*b2*b3 - a2*a3*b1*b3 - 6.0*b0*b3*a3*a3 - 3.0*a1*a2*b3*b3 - 2.0*a2*a3*b2*b2 + 2.0*b2*b3*a2*a2 + 3.0*b1*b2*a3*a3 + 6.0*a0*a3*b3*b3;
		uyy = 3.0*a1*a2*a3*b3 + a3*b2*a2*a2 - a2*b1*a3*a3 - 3.0*a0*b3*a3*a3 - 2.0*a1*b2*a3*a3 - b3*a2*a2*a2 + 3.0*b0*a3*a3*a3;
		ux = a2*a3*b0*b1*b3 - a1*a2*b1*b2*b3 - a1*a3*b0*b2*b3 + 6.0*a0*a3*b1*b2*b3 + b1*a1*a1*b3*b3 + b3*a2*a2*b1*b1 + 3.0*b3*a3*a3*b0*b0 + a1*a3*b1*b2*b2 - a2*a3*b2*b1*b1 - 6.0*a0*a3*b0*b3*b3 - 4.0*a0*a2*b1*b3*b3 - 3.0*b0*b1*b2*a3*a3 - 2.0*a0*a1*b2*b3*b3 - 2.0*a1*a3*b3*b1*b1 - 2.0*b0*b2*b3*a2*a2 + 2.0*a0*a2*b3*b2*b2 + 2.0*a2*a3*b0*b2*b2 + 3.0*a1*a2*b0*b3*b3 + a3*a3*b1*b1*b1 + 3.0*a0*a0*b3*b3*b3 - 2.0*a0*a3*b2*b2*b2;
		uy = a0*a2*a3*b1*b3 + a1*a2*a3*b1*b2 - a0*a1*a3*b2*b3 - 6.0*a1*a2*a3*b0*b3 - a1*a1*a1*b3*b3 - 3.0*a3*a3*a3*b0*b0 - a1*a3*a3*b1*b1 - a3*a1*a1*b2*b2 - 3.0*a3*a0*a0*b3*b3 + a2*b2*b3*a1*a1 - a1*b1*b3*a2*a2 - 3.0*a0*b1*b2*a3*a3 - 2.0*a0*b2*b3*a2*a2 - 2.0*a3*b0*b2*a2*a2 + 2.0*a0*a2*a3*b2*b2 + 2.0*a2*b0*b1*a3*a3 + 2.0*a3*b1*b3*a1*a1 + 3.0*a0*a1*a2*b3*b3 + 4.0*a1*b0*b2*a3*a3 + 6.0*a0*b0*b3*a3*a3 + 2.0*b0*b3*a2*a2*a2;
		u0 = a0*a1*a2*b1*b2*b3 + a0*a1*a3*b0*b2*b3 - a0*a2*a3*b0*b1*b3 - a1*a2*a3*b0*b1*b2 + b0*a1*a1*a1*b3*b3 - b3*a2*a2*a2*b0*b0 + a1*b0*a3*a3*b1*b1 + a1*b2*a0*a0*b3*b3 + a3*b0*a1*a1*b2*b2 + a3*b2*a2*a2*b0*b0 - a0*b1*a1*a1*b3*b3 - a0*b3*a2*a2*b1*b1 - a2*b1*a3*a3*b0*b0 - a2*b3*a0*a0*b2*b2 - 3.0*a0*b3*a3*a3*b0*b0 - 2.0*a1*b2*a3*a3*b0*b0 + 2.0*a2*b1*a0*a0*b3*b3 + 3.0*a3*b0*a0*a0*b3*b3 + a0*a2*a3*b2*b1*b1 + a1*b0*b1*b3*a2*a2 - a0*a1*a3*b1*b2*b2 - a2*b0*b2*b3*a1*a1 - 3.0*a0*a1*a2*b0*b3*b3 - 3.0*a3*b1*b2*b3*a0*a0 - 2.0*a0*a2*a3*b0*b2*b2 - 2.0*a3*b0*b1*b3*a1*a1 + 2.0*a0*a1*a3*b3*b1*b1 + 2.0*a0*b0*b2*b3*a2*a2 + 3.0*a0*b0*b1*b2*a3*a3 + 3.0*a1*a2*a3*b3*b0*b0 + a3*a3*a3*b0*b0*b0 - a0*a0*a0*b3*b3*b3 + a3*a0*a0*b2*b2*b2 - a0*a3*a3*b1*b1*b1;

		System.out.printf("f(x,y) = %gx^3 + %gx^2y + %gxy^2 + %gy^3 + %gx^2 + %gxy + %gy^2 + %gx + %gy + %g\n", uxxx, uxxy, uxyy, uyyy, uxx, uxy, uyy, ux, uy, u0);
		System.out.println();

		this.x1 = x1;
		this.y1 = y1;
		this.cx1 = cx1;
		this.cy1 = cy1;
		this.cx2 = cx2;
		this.cy2 = cy2;
		this.x2 = x2;
		this.y2 = y2;
	}
	
	public Point pointAt(double t) {
		return new Point(a0 + t*(a1 + t*(a2 + t*a3)), b0 + t*(b1 + t*(b2 + t*b3)));
	}

	public Set<BezierPoint> selfIntersections() {
		Set<BezierPoint> selfIntersects = new HashSet<BezierPoint>();
		
		double u2 = -2.0*a2*a3*b2*b3 + a2*a2*b3*b3 + a3*a3*b2*b2;
		double u1 = -a1*a3*b2*b3 - a2*a3*b1*b3 + a1*a2*b3*b3 + b1*b2*a3*a3;
		double u0 = -a1*a2*b2*b3 - a2*a3*b1*b2 - 2*a1*a3*b1*b3 + a1*a1*b3*b3 + a3*a3*b1*b1 + a1*a3*b2*b2 + b1*b3*a2*a2;
		double disc = u1*u1 - 4.0*u2*u0;
		if (disc < 0) return selfIntersects;
		double t1 = (-u1 - Math.sqrt(disc)) / (2.0*u2);
		double t2 = (-u1 + Math.sqrt(disc)) / (2.0*u2);
		//System.out.println("Self intersection roots at " + t1 + " and " + t2);
		//System.out.println();
		selfIntersects.add(new BezierPoint(this, t1));
		selfIntersects.add(new BezierPoint(this, t2));
		
		return selfIntersects;
	}

	public Set<BezierPoint> intersections(BezierCurve other) {
		Set<BezierPoint> intersections = new HashSet<BezierPoint>();

		//We use the implicit form of the "other" curve, and the parametric
		//form of the "this" curve

		double ouxxx = other.uxxx;
		double ouxxy = other.uxxy;
		double ouxyy = other.uxyy;
		double ouyyy = other.uyyy;
		double ouxx = other.uxx;
		double ouxy = other.uxy;
		double ouyy = other.uyy;
		double oux = other.ux;
		double ouy = other.uy;
		double ou0 = other.u0;
		
		double[][] sturmChain = new double[10][10];
		int[] degrees = new int[10];
		
		//Set up the initial values of the Sturm chain
		sturmChain[0][9] = a3*ouxyy*b3*b3 + b3*ouxxy*a3*a3 + ouxxx*a3*a3*a3 + ouyyy*b3*b3*b3;
		sturmChain[0][8] = 2.0*a2*a3*b3*ouxxy + 2.0*a3*b2*b3*ouxyy + a2*ouxyy*b3*b3 + b2*ouxxy*a3*a3 + 3.0*a2*ouxxx*a3*a3 + 3.0*b2*ouyyy*b3*b3;
		sturmChain[0][7] = 2.0*a1*a3*b3*ouxxy + 2.0*a2*a3*b2*ouxxy + 2.0*a2*b2*b3*ouxyy + 2.0*a3*b1*b3*ouxyy + a1*ouxyy*b3*b3 + a3*ouxyy*b2*b2 + b1*ouxxy*a3*a3 + b3*ouxxy*a2*a2 + 3.0*a1*ouxxx*a3*a3 + 3.0*a3*ouxxx*a2*a2 + 3.0*b1*ouyyy*b3*b3 + 3.0*b3*ouyyy*b2*b2;
		sturmChain[0][6] = a3*b3*ouxy + 2.0*a0*a3*b3*ouxxy + 2.0*a1*a2*b3*ouxxy + 2.0*a1*a3*b2*ouxxy + 2.0*a1*b2*b3*ouxyy + 2.0*a2*a3*b1*ouxxy + 2.0*a2*b1*b3*ouxyy + 2.0*a3*b0*b3*ouxyy + 2.0*a3*b1*b2*ouxyy + 6.0*a1*a2*a3*ouxxx + 6.0*b1*b2*b3*ouyyy + ouxx*a3*a3 + ouyy*b3*b3 + a0*ouxyy*b3*b3 + a2*ouxyy*b2*b2 + b0*ouxxy*a3*a3 + b2*ouxxy*a2*a2 + 3.0*a0*ouxxx*a3*a3 + 3.0*b0*ouyyy*b3*b3 + ouxxx*a2*a2*a2 + ouyyy*b2*b2*b2;
		sturmChain[0][5] = a2*b3*ouxy + a3*b2*ouxy + 2.0*a2*a3*ouxx + 2.0*b2*b3*ouyy + 2.0*a0*a2*b3*ouxxy + 2.0*a0*a3*b2*ouxxy + 2.0*a0*b2*b3*ouxyy + 2.0*a1*a2*b2*ouxxy + 2.0*a1*a3*b1*ouxxy + 2.0*a1*b1*b3*ouxyy + 2.0*a2*a3*b0*ouxxy + 2.0*a2*b0*b3*ouxyy + 2.0*a2*b1*b2*ouxyy + 2.0*a3*b0*b2*ouxyy + 6.0*a0*a2*a3*ouxxx + 6.0*b0*b2*b3*ouyyy + a1*ouxyy*b2*b2 + a3*ouxyy*b1*b1 + b1*ouxxy*a2*a2 + b3*ouxxy*a1*a1 + 3.0*a1*ouxxx*a2*a2 + 3.0*a3*ouxxx*a1*a1 + 3.0*b1*ouyyy*b2*b2 + 3.0*b3*ouyyy*b1*b1;
		sturmChain[0][4] = a1*b3*ouxy + a2*b2*ouxy + a3*b1*ouxy + 2.0*a1*a3*ouxx + 2.0*b1*b3*ouyy + 2.0*a0*a1*b3*ouxxy + 2.0*a0*a2*b2*ouxxy + 2.0*a0*a3*b1*ouxxy + 2.0*a0*b1*b3*ouxyy + 2.0*a1*a2*b1*ouxxy + 2.0*a1*a3*b0*ouxxy + 2.0*a1*b0*b3*ouxyy + 2.0*a1*b1*b2*ouxyy + 2.0*a2*b0*b2*ouxyy + 2.0*a3*b0*b1*ouxyy + 6.0*a0*a1*a3*ouxxx + 6.0*b0*b1*b3*ouyyy + ouxx*a2*a2 + ouyy*b2*b2 + a0*ouxyy*b2*b2 + a2*ouxyy*b1*b1 + b0*ouxxy*a2*a2 + b2*ouxxy*a1*a1 + 3.0*a0*ouxxx*a2*a2 + 3.0*a2*ouxxx*a1*a1 + 3.0*b0*ouyyy*b2*b2 + 3.0*b2*ouyyy*b1*b1;
		sturmChain[0][3] = a3*oux + b3*ouy + a0*b3*ouxy + a1*b2*ouxy + a2*b1*ouxy + a3*b0*ouxy + 2.0*a0*a3*ouxx + 2.0*a1*a2*ouxx + 2.0*b0*b3*ouyy + 2.0*b1*b2*ouyy + 2.0*a0*a1*b2*ouxxy + 2.0*a0*a2*b1*ouxxy + 2.0*a0*a3*b0*ouxxy + 2.0*a0*b0*b3*ouxyy + 2.0*a0*b1*b2*ouxyy + 2.0*a1*a2*b0*ouxxy + 2.0*a1*b0*b2*ouxyy + 2.0*a2*b0*b1*ouxyy + 6.0*a0*a1*a2*ouxxx + 6.0*b0*b1*b2*ouyyy + a1*ouxyy*b1*b1 + a3*ouxyy*b0*b0 + b1*ouxxy*a1*a1 + b3*ouxxy*a0*a0 + 3.0*a3*ouxxx*a0*a0 + 3.0*b3*ouyyy*b0*b0 + ouxxx*a1*a1*a1 + ouyyy*b1*b1*b1;
		sturmChain[0][2] = a2*oux + b2*ouy + a0*b2*ouxy + a1*b1*ouxy + a2*b0*ouxy + 2.0*a0*a2*ouxx + 2.0*b0*b2*ouyy + 2.0*a0*a1*b1*ouxxy + 2.0*a0*a2*b0*ouxxy + 2.0*a0*b0*b2*ouxyy + 2.0*a1*b0*b1*ouxyy + ouxx*a1*a1 + ouyy*b1*b1 + a0*ouxyy*b1*b1 + a2*ouxyy*b0*b0 + b0*ouxxy*a1*a1 + b2*ouxxy*a0*a0 + 3.0*a0*ouxxx*a1*a1 + 3.0*a2*ouxxx*a0*a0 + 3.0*b0*ouyyy*b1*b1 + 3.0*b2*ouyyy*b0*b0;
		sturmChain[0][1] = a1*oux + b1*ouy + a0*b1*ouxy + a1*b0*ouxy + 2.0*a0*a1*ouxx + 2.0*b0*b1*ouyy + 2.0*a0*a1*b0*ouxxy + 2.0*a0*b0*b1*ouxyy + a1*ouxyy*b0*b0 + b1*ouxxy*a0*a0 + 3.0*a1*ouxxx*a0*a0 + 3.0*b1*ouyyy*b0*b0;
		sturmChain[0][0] = ou0 + a0*oux + b0*ouy + a0*b0*ouxy + ouxx*a0*a0 + ouyy*b0*b0 + a0*ouxyy*b0*b0 + b0*ouxxy*a0*a0 + ouxxx*a0*a0*a0 + ouyyy*b0*b0*b0;
		int initialDegree;
		for (initialDegree = 9; Math.abs(sturmChain[0][initialDegree]) == 0.0; initialDegree--) ;
		degrees[0] = initialDegree;
		
		//Compute the derivative of the initial function
		for (int i = 0; i < degrees[0]; i++) {
			sturmChain[1][i] = (double) (i + 1) * sturmChain[0][i + 1];
		}
		degrees[1] = Math.max(0, degrees[0] - 1);
		
		//Then iteratively compute the next polynomial in the chain
		int chainSize;
		for (chainSize = 1; degrees[chainSize] > 0; chainSize++) {
			int current = chainSize;
			int prev = current - 1;
			int next = current + 1;
			double divisor = sturmChain[current][degrees[current]];
			double prev_degree = sturmChain[prev][degrees[prev]];
			double term1const = (prev_degree*sturmChain[current][degrees[current] - 1]) / (divisor*divisor);
			double term2const = prev_degree / divisor;
			double term3const = sturmChain[prev][degrees[prev] - 1] / divisor;
			sturmChain[next][0] = -sturmChain[prev][0] - term1const*sturmChain[current][0] + term3const*sturmChain[current][0];
			for (int i = 1; i < degrees[current]; i++) {
				sturmChain[next][i] = -sturmChain[prev][i] - term1const*sturmChain[current][i] + term2const*sturmChain[current][i - 1] + term3const*sturmChain[current][i];
			}
			int degree;
			for (degree = degrees[current]; Math.abs(sturmChain[next][degree]) == 0.0; degree--) ;
			degrees[next] = degree;
		}
		//System.out.println("Sturm coefficients are:\n" + Arrays.deepToString(sturmChain).replace("], [", "]\n["));
		//System.out.println();

		//Valid roots are only those in the t range [0, 1], so we start our
		//search range as that
		Set<Double> roots = new HashSet<Double>();
		Map<Double, Double> ranges = new HashMap<Double, Double>();
		ranges.put(0.0, 1.0);

		while (!ranges.isEmpty()) {
			double start = ranges.keySet().toArray(new Double[0])[0];
			double end = ranges.get(start);

			//We can remove this range from the queue
			ranges.remove(start);

			//Evaluate the polynomials using a Horner scheme
			double[] startvals = new double[chainSize + 1];
			double[] endvals = new double[chainSize + 1];
			for (int i = 0; i <= chainSize; i++) {
				double startval = 0.0, endval = 0.0;
				for (int deg = degrees[i]; deg > 0; deg--) {
					double add = sturmChain[i][deg];
					startval += add;
					startval *= start;
					endval += add;
					endval *= end;
				}
				startvals[i] = startval + sturmChain[i][0];
				endvals[i] = endval + sturmChain[i][0];
			}
			
			//Count the number of sign changes between each polynomial
			int startsignchanges = 0, endsignchanges = 0;
			for (int i = 0; i < chainSize; i++) {
				if ((startvals[i] >= 0.0) == (startvals[i + 1] >= 0.0)) {
					startsignchanges++;
				}
				if ((endvals[i] >= 0.0) == (endvals[i + 1] >= 0.0)) {
					endsignchanges++;
				}
			}
			
			//The number of roots in the range is the difference between the
			//sign change counts
			int nroots = Math.abs(startsignchanges - endsignchanges);
			if (nroots > 0) {
				double mid = (start + end) / 2.0;
				if (Math.abs(start - end) < EPSILON) {
					roots.add(mid);
				} else {
					ranges.put(start, mid);
					ranges.put(mid, end);
				}
			}
		}
		//System.out.println("Intersection roots at " + roots);
		//System.out.println();

		for (double t : roots) {
			//Remember, the t values for the roots are on this curve, not the other
			intersections.add(new BezierPoint(this, t));
		}
		return intersections;
	}
	
	public Bounds getControlBounds() {
		return new Bounds(Math.min(Math.min(x1, x2), Math.min(cx1, cx2)), Math.max(Math.max(x1, x2), Math.max(cx1, cx2)), Math.min(Math.min(y1, y2), Math.min(cy1, cy2)), Math.max(Math.max(y1, y2), Math.max(cy1, cy2)));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(cx1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(cx2);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(cy1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(cy2);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(x1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(x2);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y2);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BezierCurve other = (BezierCurve) obj;
		if (Double.doubleToLongBits(cx1) != Double.doubleToLongBits(other.cx1))
			return false;
		if (Double.doubleToLongBits(cx2) != Double.doubleToLongBits(other.cx2))
			return false;
		if (Double.doubleToLongBits(cy1) != Double.doubleToLongBits(other.cy1))
			return false;
		if (Double.doubleToLongBits(cy2) != Double.doubleToLongBits(other.cy2))
			return false;
		if (Double.doubleToLongBits(x1) != Double.doubleToLongBits(other.x1))
			return false;
		if (Double.doubleToLongBits(x2) != Double.doubleToLongBits(other.x2))
			return false;
		if (Double.doubleToLongBits(y1) != Double.doubleToLongBits(other.y1))
			return false;
		if (Double.doubleToLongBits(y2) != Double.doubleToLongBits(other.y2))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + x1 + ", " + y1 + ")--(" + cx1 + ", " + cy1 + ")--(" + cx2 + ", " + cy2 + ")--(" + x2 + ", " + y2 + ")";
	}

	public static void main(String[] args) {
		//Nine intersections here
		//BezierCurve p = new BezierCurve(0, 1000, 333, -2000, 666, 3000, 1000, 0);
		//BezierCurve q = new BezierCurve(0, 0, 3000, 333, -2000, 666, 1000, 1000);

		//Self intersection tests
		BezierCurve p = new BezierCurve(0, 1000, 1500, -300, -500, -300, 750, 1000);
		BezierCurve q = new BezierCurve(0, 0, 1750, 1500, -1000, 1000, 1000, 0);
		System.out.println("P = " + p);
		System.out.println("Q = " + q);
		
		long startTime = System.currentTimeMillis();
		BezierIntersection selfIntersectionP = BezierIntersection.createSelfIntersection(p);
		BezierIntersection selfIntersectionQ = BezierIntersection.createSelfIntersection(q);
		Set<BezierPoint> intersections = p.intersections(q);
		System.out.println("Computation took " + (System.currentTimeMillis() - startTime) + " ms");
		System.out.println("P intersects itself at " + selfIntersectionP);
		System.out.println("Q intersects itself at " + selfIntersectionQ);
		System.out.println("P and Q intersect at " + intersections);
		
		BufferedImage img = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g.setStroke(new BasicStroke(1));
		g.draw(new CubicCurve2D.Double(p.x1, p.y1, p.cx1, p.cy1, p.cx2, p.cy2, p.x2, p.y2));
		g.draw(new CubicCurve2D.Double(q.x1, q.y1, q.cx1, q.cy1, q.cx2, q.cy2, q.x2, q.y2));
		g.setStroke(new BasicStroke(2));
		g.setColor(Color.RED);
		if (selfIntersectionP != null) {
			g.draw(new Line2D.Double(selfIntersectionP.pIntersect.x - 5.0, selfIntersectionP.pIntersect.y, selfIntersectionP.pIntersect.x + 5.0, selfIntersectionP.pIntersect.y));
			g.draw(new Line2D.Double(selfIntersectionP.pIntersect.x, selfIntersectionP.pIntersect.y - 5.0, selfIntersectionP.pIntersect.x, selfIntersectionP.pIntersect.y + 5.0));
		}
		if (selfIntersectionQ != null) {
			g.draw(new Line2D.Double(selfIntersectionQ.pIntersect.x - 5.0, selfIntersectionQ.pIntersect.y, selfIntersectionQ.pIntersect.x + 5.0, selfIntersectionQ.pIntersect.y));
			g.draw(new Line2D.Double(selfIntersectionQ.pIntersect.x, selfIntersectionQ.pIntersect.y - 5.0, selfIntersectionQ.pIntersect.x, selfIntersectionQ.pIntersect.y + 5.0));
		}
		for (BezierPoint point : intersections) {
			g.draw(new Line2D.Double(point.x - 5.0, point.y, point.x + 5.0, point.y));
			g.draw(new Line2D.Double(point.x, point.y - 5.0, point.x, point.y + 5.0));
		}
		try {
			ImageIO.write(img, "PNG", new File("test.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

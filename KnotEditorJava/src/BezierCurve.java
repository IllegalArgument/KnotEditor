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

		//System.out.printf("x(t) = %gt^3 + %gt^2 + %gt + %g\n", a3, a2, a1, a0);
		//System.out.printf("y(t) = %gt^3 + %gt^2 + %gt + %g\n", b3, b2, b1, b0);

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

		//System.out.printf("f(x,y) = %gx^3 + %gx^2y + %gxy^2 + %gx^2 + %gxy + %gy^2 + %gx + %gy + %g\n", uxxx, uxxy, uxyy, uyyy, uxx, uxy, uyy, ux, uy, u0);
		//System.out.println();

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

		//This is the polynomial we need to solve to find the values of t
		//where the intersection(s) occur
		//p09*t^9 + p08*t^8 + ... + p01*t + p00 = 0

		double p09 = a3*ouxyy*b3*b3 + b3*ouxxy*a3*a3 + ouxxx*a3*a3*a3 + ouyyy*b3*b3*b3;
		double p08 = 2.0*a2*a3*b3*ouxxy + 2.0*a3*b2*b3*ouxyy + a2*ouxyy*b3*b3 + b2*ouxxy*a3*a3 + 3.0*a2*ouxxx*a3*a3 + 3.0*b2*ouyyy*b3*b3;
		double p07 = 2*a1*a3*b3*ouxxy + 2.0*a2*a3*b2*ouxxy + 2.0*a2*b2*b3*ouxyy + 2.0*a3*b1*b3*ouxyy + a1*ouxyy*b3*b3 + a3*ouxyy*b2*b2 + b1*ouxxy*a3*a3 + b3*ouxxy*a2*a2 + 3.0*a1*ouxxx*a3*a3 + 3.0*a3*ouxxx*a2*a2 + 3.0*b1*ouyyy*b3*b3 + 3.0*b3*ouyyy*b2*b2;
		double p06 = a3*b3*ouxy + 2.0*a0*a3*b3*ouxxy + 2.0*a1*a2*b3*ouxxy + 2.0*a1*a3*b2*ouxxy + 2.0*a1*b2*b3*ouxyy + 2.0*a2*a3*b1*ouxxy + 2.0*a2*b1*b3*ouxyy + 2.0*a3*b0*b3*ouxyy + 2.0*a3*b1*b2*ouxyy + 6.0*a1*a2*a3*ouxxx + 6.0*b1*b2*b3*ouyyy + ouxx*a3*a3 + ouyy*b3*b3 + a0*ouxyy*b3*b3 + a2*ouxyy*b2*b2 + b0*ouxxy*a3*a3 + b2*ouxxy*a2*a2 + 3.0*a0*ouxxx*a3*a3 + 3.0*b0*ouyyy*b3*b3 + ouxxx*a2*a2*a2 + ouyyy*b2*b2*b2;
		double p05 = a2*b3*ouxy + a3*b2*ouxy + 2.0*a2*a3*ouxx + 2.0*b2*b3*ouyy + 2.0*a0*a2*b3*ouxxy + 2.0*a0*a3*b2*ouxxy + 2.0*a0*b2*b3*ouxyy + 2.0*a1*a2*b2*ouxxy + 2.0*a1*a3*b1*ouxxy + 2.0*a1*b1*b3*ouxyy + 2.0*a2*a3*b0*ouxxy + 2.0*a2*b0*b3*ouxyy + 2.0*a2*b1*b2*ouxyy + 2.0*a3*b0*b2*ouxyy + 6.0*a0*a2*a3*ouxxx + 6.0*b0*b2*b3*ouyyy + a1*ouxyy*b2*b2 + a3*ouxyy*b1*b1 + b1*ouxxy*a2*a2 + b3*ouxxy*a1*a1 + 3.0*a1*ouxxx*a2*a2 + 3.0*a3*ouxxx*a1*a1 + 3.0*b1*ouyyy*b2*b2 + 3.0*b3*ouyyy*b1*b1;
		double p04 = a1*b3*ouxy + a2*b2*ouxy + a3*b1*ouxy + 2.0*a1*a3*ouxx + 2.0*b1*b3*ouyy + 2.0*a0*a1*b3*ouxxy + 2.0*a0*a2*b2*ouxxy + 2.0*a0*a3*b1*ouxxy + 2.0*a0*b1*b3*ouxyy + 2.0*a1*a2*b1*ouxxy + 2.0*a1*a3*b0*ouxxy + 2.0*a1*b0*b3*ouxyy + 2.0*a1*b1*b2*ouxyy + 2.0*a2*b0*b2*ouxyy + 2.0*a3*b0*b1*ouxyy + 6.0*a0*a1*a3*ouxxx + 6.0*b0*b1*b3*ouyyy + ouxx*a2*a2 + ouyy*b2*b2 + a0*ouxyy*b2*b2 + a2*ouxyy*b1*b1 + b0*ouxxy*a2*a2 + b2*ouxxy*a1*a1 + 3.0*a0*ouxxx*a2*a2 + 3.0*a2*ouxxx*a1*a1 + 3.0*b0*ouyyy*b2*b2 + 3.0*b2*ouyyy*b1*b1;
		double p03 = a3*oux + b3*ouy + a0*b3*ouxy + a1*b2*ouxy + a2*b1*ouxy + a3*b0*ouxy + 2.0*a0*a3*ouxx + 2.0*a1*a2*ouxx + 2.0*b0*b3*ouyy + 2.0*b1*b2*ouyy + 2.0*a0*a1*b2*ouxxy + 2.0*a0*a2*b1*ouxxy + 2.0*a0*a3*b0*ouxxy + 2.0*a0*b0*b3*ouxyy + 2.0*a0*b1*b2*ouxyy + 2.0*a1*a2*b0*ouxxy + 2.0*a1*b0*b2*ouxyy + 2.0*a2*b0*b1*ouxyy + 6.0*a0*a1*a2*ouxxx + 6.0*b0*b1*b2*ouyyy + a1*ouxyy*b1*b1 + a3*ouxyy*b0*b0 + b1*ouxxy*a1*a1 + b3*ouxxy*a0*a0 + 3.0*a3*ouxxx*a0*a0 + 3.0*b3*ouyyy*b0*b0 + ouxxx*a1*a1*a1 + ouyyy*b1*b1*b1;
		double p02 = a2*oux + b2*ouy + a0*b2*ouxy + a1*b1*ouxy + a2*b0*ouxy + 2.0*a0*a2*ouxx + 2.0*b0*b2*ouyy + 2.0*a0*a1*b1*ouxxy + 2.0*a0*a2*b0*ouxxy + 2.0*a0*b0*b2*ouxyy + 2.0*a1*b0*b1*ouxyy + ouxx*a1*a1 + ouyy*b1*b1 + a0*ouxyy*b1*b1 + a2*ouxyy*b0*b0 + b0*ouxxy*a1*a1 + b2*ouxxy*a0*a0 + 3.0*a0*ouxxx*a1*a1 + 3.0*a2*ouxxx*a0*a0 + 3.0*b0*ouyyy*b1*b1 + 3.0*b2*ouyyy*b0*b0;
		double p01 = a1*oux + b1*ouy + a0*b1*ouxy + a1*b0*ouxy + 2.0*a0*a1*ouxx + 2.0*b0*b1*ouyy + 2.0*a0*a1*b0*ouxxy + 2.0*a0*b0*b1*ouxyy + a1*ouxyy*b0*b0 + b1*ouxxy*a0*a0 + 3.0*a1*ouxxx*a0*a0 + 3.0*b1*ouyyy*b0*b0;
		double p00 = ou0 + a0*oux + b0*ouy + a0*b0*ouxy + ouxx*a0*a0 + ouyy*b0*b0 + a0*ouxyy*b0*b0 + b0*ouxxy*a0*a0 + ouxxx*a0*a0*a0 + ouyyy*b0*b0*b0;

		//System.out.printf("p0(t) = %gt^9 + %gt^8 + %gt^7 + %gt^6 + %gt^5 + %gt^4 + %gt^3 + %gt^2 + %gt + %g\n", p09, p08, p07, p06, p05, p04, p03, p02, p01, p00);

		//We do so using Sturm's method

		double p18 = 9.0*p09;
		double p17 = 8.0*p08;
		double p16 = 7.0*p07;
		double p15 = 6.0*p06;
		double p14 = 5.0*p05;
		double p13 = 4.0*p04;
		double p12 = 3.0*p03;
		double p11 = 2.0*p02;
		double p10 = p01;

		//System.out.printf("p1(t) = %gt^8 + %gt^7 + %gt^6 + %gt^5 + %gt^4 + %gt^3 + %gt^2 + %gt + %g\n", p18, p17, p16, p15, p14, p13, p12, p11, p10);

		double p20 = -(p00 + (p09*p10*p17) / (p18*p18) - (p08*p10) / p18);
		double p21 = -(p01 + (p09*p11*p17) / (p18*p18) - (p09*p10) / p18 - (p08*p11) / p18);
		double p22 = -(p02 + (p09*p12*p17) / (p18*p18) - (p09*p11) / p18 - (p08*p12) / p18);
		double p23 = -(p03 + (p09*p13*p17) / (p18*p18) - (p09*p12) / p18 - (p08*p13) / p18);
		double p24 = -(p04 + (p09*p14*p17) / (p18*p18) - (p09*p13) / p18 - (p08*p14) / p18);
		double p25 = -(p05 + (p09*p15*p17) / (p18*p18) - (p09*p14) / p18 - (p08*p15) / p18);
		double p26 = -(p06 + (p09*p16*p17) / (p18*p18) - (p09*p15) / p18 - (p08*p16) / p18);
		double p27 = -(p07 + (p09*p17*p17) / (p18*p18) - (p09*p16) / p18 - (p08*p17) / p18);

		//System.out.printf("p2(t) = %gt^7 + %gt^6 + %gt^5 + %gt^4 + %gt^3 + %gt^2 + %gt + %g\n", p27, p26, p25, p24, p23, p22, p21, p20);

		double p30 = -(p10 + (p18*p20*p26) / (p27*p27) - (p17*p20) / p27);
		double p31 = -(p11 + (p18*p21*p26) / (p27*p27) - (p18*p20) / p27 - (p17*p21) / p27);
		double p32 = -(p12 + (p18*p22*p26) / (p27*p27) - (p18*p21) / p27 - (p17*p22) / p27);
		double p33 = -(p13 + (p18*p23*p26) / (p27*p27) - (p18*p22) / p27 - (p17*p23) / p27);
		double p34 = -(p14 + (p18*p24*p26) / (p27*p27) - (p18*p23) / p27 - (p17*p24) / p27);
		double p35 = -(p15 + (p18*p25*p26) / (p27*p27) - (p18*p24) / p27 - (p17*p25) / p27);
		double p36 = -(p16 + (p18*p26*p26) / (p27*p27) - (p18*p25) / p27 - (p17*p26) / p27);

		//System.out.printf("p3(t) = %gt^6 + %gt^5 + %gt^4 + %gt^3 + %gt^2 + %gt + %g\n", p36, p35, p34, p33, p32, p31, p30);

		double p40 = -(p20 + (p27*p30*p35) / (p36*p36) - (p26*p30) / p36);
		double p41 = -(p21 + (p27*p31*p35) / (p36*p36) - (p27*p30) / p36 - (p26*p31) / p36);
		double p42 = -(p22 + (p27*p32*p35) / (p36*p36) - (p27*p31) / p36 - (p26*p32) / p36);
		double p43 = -(p23 + (p27*p33*p35) / (p36*p36) - (p27*p32) / p36 - (p26*p33) / p36);
		double p44 = -(p24 + (p27*p34*p35) / (p36*p36) - (p27*p33) / p36 - (p26*p34) / p36);
		double p45 = -(p25 + (p27*p35*p35) / (p36*p36) - (p27*p34) / p36 - (p26*p35) / p36);

		//System.out.printf("p4(t) = %gt^5 + %gt^4 + %gt^3 + %gt^2 + %gt + %g\n", p45, p44, p43, p42, p41, p40);

		double p50 = -(p30 + (p36*p40*p44) / (p45*p45) - (p35*p40) / p45);
		double p51 = -(p31 + (p36*p41*p44) / (p45*p45) - (p36*p40) / p45 - (p35*p41) / p45);
		double p52 = -(p32 + (p36*p42*p44) / (p45*p45) - (p36*p41) / p45 - (p35*p42) / p45);
		double p53 = -(p33 + (p36*p43*p44) / (p45*p45) - (p36*p42) / p45 - (p35*p43) / p45);
		double p54 = -(p34 + (p36*p44*p44) / (p45*p45) - (p36*p43) / p45 - (p35*p44) / p45);

		//System.out.printf("p5(t) = %gt^4 + %gt^3 + %gt^2 + %gt + %g\n", p54, p53, p52, p51, p50);

		double p60 = -(p40 + (p45*p50*p53) / (p54*p54) - (p44*p50) / p54);
		double p61 = -(p41 + (p45*p51*p53) / (p54*p54) - (p45*p50) / p54 - (p44*p51) / p54);
		double p62 = -(p42 + (p45*p52*p53) / (p54*p54) - (p45*p51) / p54 - (p44*p52) / p54);
		double p63 = -(p43 + (p45*p53*p53) / (p54*p54) - (p45*p52) / p54 - (p44*p53) / p54);

		//System.out.printf("p6(t) = %gt^3 + %gt^2 + %gt + %g\n", p63, p62, p61, p60);

		double p70 = -(p50 + (p54*p60*p62) / (p63*p63) - (p53*p60) / p63);
		double p71 = -(p51 + (p54*p61*p62) / (p63*p63) - (p54*p60) / p63 - (p53*p61) / p63);
		double p72 = -(p52 + (p54*p62*p62) / (p63*p63) - (p54*p61) / p63 - (p53*p62) / p63);

		//System.out.printf("p7(t) = %gt^2 + %gt + %g\n", p72, p71, p70);

		double p80 = -(p60 + (p63*p70*p71) / (p72*p72) - (p62*p70) / p72);
		double p81 = -(p61 + (p63*p71*p71) / (p72*p72) - (p63*p70) / p72 - (p62*p71) / p72);

		//System.out.printf("p8(t) = %gt + %g\n", p81, p80);

		double p90 = -(p70 + (p72*p80*p80) / (p81*p81) - (p71*p80) / p81);

		//System.out.printf("p9(t) = %g\n", p90);
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
			double startp0 = p00 + start*(p01 + start*(p02 + start*(p03 + start*(p04 + start*(p05 + start*(p06 + start*(p07 + start*(p08 + start*p09))))))));
			double startp1 = p10 + start*(p11 + start*(p12 + start*(p13 + start*(p14 + start*(p15 + start*(p16 + start*(p17 + start*p18)))))));
			double startp2 = p20 + start*(p21 + start*(p22 + start*(p23 + start*(p24 + start*(p25 + start*(p26 + start*p27))))));
			double startp3 = p30 + start*(p31 + start*(p32 + start*(p33 + start*(p34 + start*(p35 + start*p36)))));
			double startp4 = p40 + start*(p41 + start*(p42 + start*(p43 + start*(p44 + start*p45))));
			double startp5 = p50 + start*(p51 + start*(p52 + start*(p53 + start*p54)));
			double startp6 = p60 + start*(p61 + start*(p62 + start*p63));
			double startp7 = p70 + start*(p71 + start*p72);
			double startp8 = p80 + start*p81;
			double startp9 = p90;
			double endp0 = p00 + end*(p01 + end*(p02 + end*(p03 + end*(p04 + end*(p05 + end*(p06 + end*(p07 + end*(p08 + end*p09))))))));
			double endp1 = p10 + end*(p11 + end*(p12 + end*(p13 + end*(p14 + end*(p15 + end*(p16 + end*(p17 + end*p18)))))));
			double endp2 = p20 + end*(p21 + end*(p22 + end*(p23 + end*(p24 + end*(p25 + end*(p26 + end*p27))))));
			double endp3 = p30 + end*(p31 + end*(p32 + end*(p33 + end*(p34 + end*(p35 + end*p36)))));
			double endp4 = p40 + end*(p41 + end*(p42 + end*(p43 + end*(p44 + end*p45))));
			double endp5 = p50 + end*(p51 + end*(p52 + end*(p53 + end*p54)));
			double endp6 = p60 + end*(p61 + end*(p62 + end*p63));
			double endp7 = p70 + end*(p71 + end*p72);
			double endp8 = p80 + end*p81;
			double endp9 = p90;

			//And count the number of sign changes that occur in the range
			int startsignchange = ((startp0 >= 0.0) == (startp1 >= 0.0) ? 0 : 1) + ((startp1 >= 0.0) == (startp2 >= 0.0) ? 0 : 1) + ((startp2 >= 0.0) == (startp3 >= 0.0) ? 0 : 1) + ((startp3 >= 0.0) == (startp4 >= 0.0) ? 0 : 1) + ((startp4 >= 0.0) == (startp5 >= 0.0) ? 0 : 1) + ((startp5 >= 0.0) == (startp6 >= 0.0) ? 0 : 1) + ((startp6 >= 0.0) == (startp7 >= 0.0) ? 0 : 1) + ((startp7 >= 0.0) == (startp8 >= 0.0) ? 0 : 1) + ((startp8 >= 0.0) == (startp9 >= 0.0) ? 0 : 1);
			int endsignchange = ((endp0 >= 0.0) == (endp1 >= 0.0) ? 0 : 1) + ((endp1 >= 0.0) == (endp2 >= 0.0) ? 0 : 1) + ((endp2 >= 0.0) == (endp3 >= 0.0) ? 0 : 1) + ((endp3 >= 0.0) == (endp4 >= 0.0) ? 0 : 1) + ((endp4 >= 0.0) == (endp5 >= 0.0) ? 0 : 1) + ((endp5 >= 0.0) == (endp6 >= 0.0) ? 0 : 1) + ((endp6 >= 0.0) == (endp7 >= 0.0) ? 0 : 1) + ((endp7 >= 0.0) == (endp8 >= 0.0) ? 0 : 1) + ((endp8 >= 0.0) == (endp9 >= 0.0) ? 0 : 1);

			//The number of roots in the range is the difference between the
			//sign change counts
			int nroots = Math.abs(startsignchange - endsignchange);
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
			//Again, use a Horner scheme to evaluate the polynomials for the curve
			//Remember that we need to use the parametric equation of the "this"
			//curve for this, since the "other" curve was implicitized
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
		return "(" + x1 + ", " + x1 + ")--(" + cx1 + ", " + cy1 + ")--(" + cx2 + ", " + cy2 + ")--(" + x2 + ", " + y2 + ")";
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

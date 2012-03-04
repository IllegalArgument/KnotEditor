import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;


public final class CurveUtils {

	public static Set<Point2D> intersections(CubicCurve2D p, CubicCurve2D q) {
		Set<Point2D> result = new HashSet<Point2D>();

		BufferedImage img = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.scale(50, 50);
		g.translate(5, 5);
		g.setStroke(new BasicStroke(0.1f));
		g.draw(p);
		g.draw(q);
		g.setStroke(new BasicStroke(0));
		g.draw(new Line2D.Double(p.getP1(), p.getCtrlP1()));
		g.draw(new Line2D.Double(p.getP1(), p.getCtrlP2()));
		g.draw(new Line2D.Double(p.getCtrlP1(), p.getP2()));
		g.draw(new Line2D.Double(p.getCtrlP2(), p.getP2()));
		g.draw(new Line2D.Double(q.getP1(), q.getCtrlP1()));
		g.draw(new Line2D.Double(q.getP1(), q.getCtrlP2()));
		g.draw(new Line2D.Double(q.getCtrlP1(), q.getP2()));
		g.draw(new Line2D.Double(q.getCtrlP2(), q.getP2()));

		for (int i = 0; i < 4; i++) {
			double dxNormP = p.getX2() - p.getX1();
			double dyNormP = p.getY2() - p.getY1();
			double scaleP = Math.sqrt(dxNormP*dxNormP + dyNormP*dyNormP);
			dxNormP /= scaleP;
			dyNormP /= scaleP;
			double aP = dyNormP;
			double bP = -dxNormP;
			double cP = (-p.getX1()*p.getY2() + p.getX2()*p.getY1()) / scaleP;
			double d1P = aP*p.getCtrlX1() + bP*p.getCtrlY1() + cP;
			double d2P = aP*p.getCtrlX2() + bP*p.getCtrlY2() + cP;
			double dminP, dmaxP;
			if (d1P*d2P > 0) {
				dminP = 0.75 * Math.min(0, Math.min(d1P, d2P));
				dmaxP = 0.75 * Math.max(0, Math.max(d1P, d2P));

			} else {
				dminP = 4.0/9.0 * Math.min(0, Math.min(d1P, d2P));
				dmaxP = 4.0/9.0 * Math.max(0, Math.max(d1P, d2P));
			}
			System.out.println("dminP=" + dminP + " and dmaxP=" + dmaxP);

			double dist0Q = aP*q.getX1() + bP*q.getY1() + cP;
			double dist1Q = aP*q.getCtrlX1() + bP*q.getCtrlY1() + cP;
			double dist2Q = aP*q.getCtrlX2() + bP*q.getCtrlY2() + cP;
			double dist3Q = aP*q.getX2() + bP*q.getY2() + cP;

			double tmin01Q = (dminP - dist0Q) * (1.0/3.0 - 0.0) / (dist1Q - dist0Q);
			double tmin02Q = (dminP - dist0Q) * (2.0/3.0 - 0.0) / (dist2Q - dist0Q);
			tmin01Q = (tmin01Q >= 0.0 && tmin01Q <= 1.0/3.0) ? tmin01Q : 1.0;
			tmin02Q = (tmin02Q >= 0.0 && tmin02Q <= 2.0/3.0) ? tmin02Q : 1.0;
			double tmax31Q = (dmaxP - dist3Q) * (1.0/3.0 - 1.0) / (dist1Q - dist3Q) + 1.0;
			double tmax32Q = (dmaxP - dist3Q) * (2.0/3.0 - 1.0) / (dist2Q - dist3Q) + 1.0;
			tmax31Q = (tmax31Q >= 1.0/3.0 && tmax31Q <= 1.0) ? tmax31Q : 0.0;
			tmax32Q = (tmax32Q >= 2.0/3.0 && tmax32Q <= 1.0) ? tmax32Q : 0.0;
			double tminQ = (Math.min(tmin01Q, tmin02Q) >= 1.0) ? 0.0 : Math.min(tmin01Q, tmin02Q);
			double tmaxQ = (Math.max(tmax31Q, tmax32Q) <= 0.0) ? 1.0 : Math.max(tmax31Q, tmax32Q);

			System.out.println("Trimming q between t=" + tminQ + " and t=" + tmaxQ);

			double mid01xminQ = (1.0 - tminQ) * q.getX1() + tminQ * q.getCtrlX1();
			double mid01yminQ = (1.0 - tminQ) * q.getY1() + tminQ * q.getCtrlY1();
			double mid12xminQ = (1.0 - tminQ) * q.getCtrlX1() + tminQ * q.getCtrlX2();
			double mid12yminQ = (1.0 - tminQ) * q.getCtrlY1() + tminQ * q.getCtrlY2();
			double mid23xminQ = (1.0 - tminQ) * q.getCtrlX2() + tminQ * q.getX2();
			double mid23yminQ = (1.0 - tminQ) * q.getCtrlY2() + tminQ * q.getY2();
			double mid0112xminQ = (1.0 - tminQ) * mid01xminQ + tminQ * mid12xminQ;
			double mid0112yminQ = (1.0 - tminQ) * mid01yminQ + tminQ * mid12yminQ;
			double mid1223xminQ = (1.0 - tminQ) * mid12xminQ + tminQ * mid23xminQ;
			double mid1223yminQ = (1.0 - tminQ) * mid12yminQ + tminQ * mid23yminQ;
			double splitxminQ = (1.0 - tminQ) * mid0112xminQ + tminQ * mid1223xminQ;
			double splityminQ = (1.0 - tminQ) * mid0112yminQ + tminQ * mid1223yminQ;

			q.setCurve(splitxminQ, splityminQ, mid1223xminQ, mid1223yminQ, mid23xminQ, mid23yminQ, q.getX2(), q.getY2());
			tmaxQ = (tmaxQ - tminQ) / (1.0 - tminQ);

			double mid01xmaxQ = (1.0 - tmaxQ) * q.getX1() + tmaxQ * q.getCtrlX1();
			double mid01ymaxQ = (1.0 - tmaxQ) * q.getY1() + tmaxQ * q.getCtrlY1();
			double mid12xmaxQ = (1.0 - tmaxQ) * q.getCtrlX1() + tmaxQ * q.getCtrlX2();
			double mid12ymaxQ = (1.0 - tmaxQ) * q.getCtrlY1() + tmaxQ * q.getCtrlY2();
			double mid23xmaxQ = (1.0 - tmaxQ) * q.getCtrlX2() + tmaxQ * q.getX2();
			double mid23ymaxQ = (1.0 - tmaxQ) * q.getCtrlY2() + tmaxQ * q.getY2();
			double mid0112xmaxQ = (1.0 - tmaxQ) * mid01xmaxQ + tmaxQ * mid12xmaxQ;
			double mid0112ymaxQ = (1.0 - tmaxQ) * mid01ymaxQ + tmaxQ * mid12ymaxQ;
			double mid1223xmaxQ = (1.0 - tmaxQ) * mid12xmaxQ + tmaxQ * mid23xmaxQ;
			double mid1223ymaxQ = (1.0 - tmaxQ) * mid12ymaxQ + tmaxQ * mid23ymaxQ;
			double splitxmaxQ = (1.0 - tmaxQ) * mid0112xmaxQ + tmaxQ * mid1223xmaxQ;
			double splitymaxQ = (1.0 - tmaxQ) * mid0112ymaxQ + tmaxQ * mid1223ymaxQ;

			q.setCurve(splitxminQ, splityminQ, mid01xmaxQ, mid01ymaxQ, mid0112xmaxQ, mid0112ymaxQ, splitxmaxQ, splitymaxQ);

			g.setStroke(new BasicStroke(0.1f));
			g.draw(new Line2D.Double(splitxminQ, splityminQ, splitxminQ, splityminQ + 1.0));
			g.draw(new Line2D.Double(splitxmaxQ, splitymaxQ, splitxmaxQ, splitymaxQ + 1.0));
			g.setColor(new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
			g.draw(q);
			g.setColor(Color.WHITE);
			g.setStroke(new BasicStroke(0));
			g.draw(new Line2D.Double(q.getP1(), q.getCtrlP1()));
			g.draw(new Line2D.Double(q.getP1(), q.getCtrlP2()));
			g.draw(new Line2D.Double(q.getCtrlP1(), q.getP2()));
			g.draw(new Line2D.Double(q.getCtrlP2(), q.getP2()));

			double dxNormQ = q.getX2() - q.getX1();
			double dyNormQ = q.getY2() - q.getY1();
			double scaleQ = Math.sqrt(dxNormQ*dxNormQ + dyNormQ*dyNormQ);
			dxNormQ /= scaleQ;
			dyNormQ /= scaleQ;
			double aQ = dyNormQ;
			double bQ = -dxNormQ;
			double cQ = (-q.getX1()*q.getY2() + q.getX2()*q.getY1()) / scaleQ;
			double d1Q = aQ*q.getCtrlX1() + bQ*q.getCtrlY1() + cQ;
			double d2Q = aQ*q.getCtrlX2() + bQ*q.getCtrlY2() + cQ;
			double dminQ, dmaxQ;
			if (d1Q*d2Q > 0) {
				dminQ = 0.75 * Math.min(0, Math.min(d1Q, d2Q));
				dmaxQ = 0.75 * Math.max(0, Math.max(d1Q, d2Q));

			} else {
				dminQ = 4.0/9.0 * Math.min(0, Math.min(d1Q, d2Q));
				dmaxQ = 4.0/9.0 * Math.max(0, Math.max(d1Q, d2Q));
			}
			System.out.println("dminQ=" + dminQ + " and dmaxQ=" + dmaxQ);

			double dist0P = aQ*p.getX1() + bQ*p.getY1() + cQ;
			double dist1P = aQ*p.getCtrlX1() + bQ*p.getCtrlY1() + cQ;
			double dist2P = aQ*p.getCtrlX2() + bQ*p.getCtrlY2() + cQ;
			double dist3P = aQ*p.getX2() + bQ*p.getY2() + cQ;

			double tmin01P = (dminQ - dist0P) * (1.0/3.0 - 0.0) / (dist1P - dist0P);
			double tmin02P = (dminQ - dist0P) * (2.0/3.0 - 0.0) / (dist2P - dist0P);
			tmin01P = (tmin01P >= 0.0 && tmin01P <= 1.0/3.0) ? tmin01P : 1.0;
			tmin02P = (tmin02P >= 0.0 && tmin02P <= 2.0/3.0) ? tmin02P : 1.0;
			double tmax31P = (dmaxQ - dist3P) * (1.0/3.0 - 1.0) / (dist1P - dist3P) + 1.0;
			double tmax32P = (dmaxQ - dist3P) * (2.0/3.0 - 1.0) / (dist2P - dist3P) + 1.0;
			tmax31P = (tmax31P >= 1.0/3.0 && tmax31P <= 1.0) ? tmax31P : 0.0;
			tmax32P = (tmax32P >= 2.0/3.0 && tmax32P <= 1.0) ? tmax32P : 0.0;
			double tminP = (Math.min(tmin01P, tmin02P) >= 1.0) ? 0.0 : Math.min(tmin01P, tmin02P);
			double tmaxP = (Math.max(tmax31P, tmax32P) <= 0.0) ? 1.0 : Math.max(tmax31P, tmax32P);

			System.out.println("Trimming p between t=" + tminP + " and t=" + tmaxP);

			double mid01xminP = (1.0 - tminP) * p.getX1() + tminP * p.getCtrlX1();
			double mid01yminP = (1.0 - tminP) * p.getY1() + tminP * p.getCtrlY1();
			double mid12xminP = (1.0 - tminP) * p.getCtrlX1() + tminP * p.getCtrlX2();
			double mid12yminP = (1.0 - tminP) * p.getCtrlY1() + tminP * p.getCtrlY2();
			double mid23xminP = (1.0 - tminP) * p.getCtrlX2() + tminP * p.getX2();
			double mid23yminP = (1.0 - tminP) * p.getCtrlY2() + tminP * p.getY2();
			double mid0112xminP = (1.0 - tminP) * mid01xminP + tminP * mid12xminP;
			double mid0112yminP = (1.0 - tminP) * mid01yminP + tminP * mid12yminP;
			double mid1223xminP = (1.0 - tminP) * mid12xminP + tminP * mid23xminP;
			double mid1223yminP = (1.0 - tminP) * mid12yminP + tminP * mid23yminP;
			double splitxminP = (1.0 - tminP) * mid0112xminP + tminP * mid1223xminP;
			double splityminP = (1.0 - tminP) * mid0112yminP + tminP * mid1223yminP;

			p.setCurve(splitxminP, splityminP, mid1223xminP, mid1223yminP, mid23xminP, mid23yminP, p.getX2(), p.getY2());
			tmaxP = (tmaxP - tminP) / (1.0 - tminP);

			double mid01xmaxP = (1.0 - tmaxP) * p.getX1() + tmaxP * p.getCtrlX1();
			double mid01ymaxP = (1.0 - tmaxP) * p.getY1() + tmaxP * p.getCtrlY1();
			double mid12xmaxP = (1.0 - tmaxP) * p.getCtrlX1() + tmaxP * p.getCtrlX2();
			double mid12ymaxP = (1.0 - tmaxP) * p.getCtrlY1() + tmaxP * p.getCtrlY2();
			double mid23xmaxP = (1.0 - tmaxP) * p.getCtrlX2() + tmaxP * p.getX2();
			double mid23ymaxP = (1.0 - tmaxP) * p.getCtrlY2() + tmaxP * p.getY2();
			double mid0112xmaxP = (1.0 - tmaxP) * mid01xmaxP + tmaxP * mid12xmaxP;
			double mid0112ymaxP = (1.0 - tmaxP) * mid01ymaxP + tmaxP * mid12ymaxP;
			double mid1223xmaxP = (1.0 - tmaxP) * mid12xmaxP + tmaxP * mid23xmaxP;
			double mid1223ymaxP = (1.0 - tmaxP) * mid12ymaxP + tmaxP * mid23ymaxP;
			double splitxmaxP = (1.0 - tmaxP) * mid0112xmaxP + tmaxP * mid1223xmaxP;
			double splitymaxP = (1.0 - tmaxP) * mid0112ymaxP + tmaxP * mid1223ymaxP;

			p.setCurve(splitxminP, splityminP, mid01xmaxP, mid01ymaxP, mid0112xmaxP, mid0112ymaxP, splitxmaxP, splitymaxP);

			g.setStroke(new BasicStroke(0.1f));
			g.draw(new Line2D.Double(splitxminP, splityminP, splitxminP + 1.0, splityminP));
			g.draw(new Line2D.Double(splitxmaxP, splitymaxP, splitxmaxP + 1.0, splitymaxP));
			g.setColor(new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
			g.draw(p);
			g.setColor(Color.WHITE);
		}
		
		System.out.println("Intersection bounds: " + q.getBounds2D().getWidth() + " by " + q.getBounds2D().getHeight());

		try {
			ImageIO.write(img, "PNG", new File("test.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public static void main(String[] args) {
		intersections(new CubicCurve2D.Double(0, 5, 3, 0, 6, 0, 5, 10), new CubicCurve2D.Double(0, 0, 1.0/3.0 * 10.0, 8, 2.0/3.0 * 10.0, 2, 10, 10));
	}

}

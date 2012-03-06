import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;


public class SVGKnotEditor {
	
	public static Set<BezierIntersection> findIntersections(List<BezierCurve> curves) {
		Set<BezierIntersection> intersections = new HashSet<BezierIntersection>();
		
		for (int i = 0; i < curves.size(); i++) {
			BezierCurve p = curves.get(i);
			BezierIntersection selfIntersect = BezierIntersection.createSelfIntersection(p);
			if (selfIntersect != null) {
				intersections.add(selfIntersect);
			}
			for (int j = i + 1; j < curves.size(); j++) {
				System.out.println("Intersecting " + p + " and " + curves.get(j));
				Set<BezierIntersection> intersects = BezierIntersection.createIntersections(p, curves.get(j));
				System.out.println("Found " + intersects.size() + " intersections");
				intersections.addAll(intersects);
			}
		}
		
		return intersections;
	}
	
	public static void main(String[] args) {
		BezierCurve p = new BezierCurve(0, 1000, 1500, 0, -500, 0, 1000, 1000);
		BezierCurve q = new BezierCurve(0, 0, 1500, 1000, -500, 1000, 1000, 0);
		BezierCurve r = new BezierCurve(20, 1000, 333, -2000, 666, 3000, 980, 0);
		BezierCurve s = new BezierCurve(0, 20, 3000, 333, -2000, 666, 1000, 980);
		BezierCurve t = new BezierCurve(10, 0, 333, 1500, 666, -500, 990, 1000);
		BezierCurve u = new BezierCurve(10, 1000, 333, -500, 666, 1500, 990, 0);
		List<BezierCurve> curves = new ArrayList<BezierCurve>();
		curves.add(p);
		curves.add(q);
		curves.add(r);
		curves.add(s);
		curves.add(t);
		curves.add(u);
		curves.add(new BezierCurve(0, 1000, 1500, 0, -500, 0, 1000, 1000));
		long start = System.currentTimeMillis();
		Set<BezierIntersection> intersections = findIntersections(curves);
		System.out.println("Computed all intersections in " + (System.currentTimeMillis() - start) + " ms");
		
		BufferedImage img = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g.setStroke(new BasicStroke(1));
		for (BezierCurve c : curves) {
			g.draw(new CubicCurve2D.Double(c.x1, c.y1, c.cx1, c.cy1, c.cx2, c.cy2, c.x2, c.y2));
		}
		g.setStroke(new BasicStroke(2));
		g.setColor(Color.RED);
		for (BezierIntersection intersect : intersections) {
			g.draw(new Line2D.Double(intersect.pIntersect.x - 5.0, intersect.pIntersect.y, intersect.pIntersect.x + 5.0, intersect.pIntersect.y));
			g.draw(new Line2D.Double(intersect.pIntersect.x, intersect.pIntersect.y - 5.0, intersect.pIntersect.x, intersect.pIntersect.y + 5.0));
		}

		try {
			ImageIO.write(img, "PNG", new File("test.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

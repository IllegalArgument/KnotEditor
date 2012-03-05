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
				intersections.addAll(BezierIntersection.createIntersections(p, curves.get(j)));
			}
		}
		
		return intersections;
	}
	
	public static void main(String[] args) {
		BezierCurve p = new BezierCurve(0, 1000, 1500, 0, -500, 0, 1000, 1000);
		BezierCurve q = new BezierCurve(0, 0, 1500, 1000, -500, 1000, 1000, 0);
		BezierCurve r = new BezierCurve(0, 1000, 333, -2000, 666, 3000, 1000, 0);
		BezierCurve s = new BezierCurve(0, 0, 3000, 333, -2000, 666, 1000, 1000);
		List<BezierCurve> curves = new ArrayList<BezierCurve>();
		curves.add(p);
		curves.add(q);
		curves.add(r);
		curves.add(s);
		Set<BezierIntersection> intersections = findIntersections(curves);
		
		BufferedImage img = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g.setStroke(new BasicStroke(1));
		g.draw(new CubicCurve2D.Double(p.x1, p.y1, p.cx1, p.cy1, p.cx2, p.cy2, p.x2, p.y2));
		g.draw(new CubicCurve2D.Double(q.x1, q.y1, q.cx1, q.cy1, q.cx2, q.cy2, q.x2, q.y2));
		g.draw(new CubicCurve2D.Double(r.x1, r.y1, r.cx1, r.cy1, r.cx2, r.cy2, r.x2, r.y2));
		g.draw(new CubicCurve2D.Double(s.x1, s.y1, s.cx1, s.cy1, s.cx2, s.cy2, s.x2, s.y2));
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

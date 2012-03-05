
public class Bounds {
	
	public final double xmin, ymin, xmax, ymax;

	public Bounds(double x1, double y1, double x2, double y2) {
		xmin = Math.min(x1, x2);
		xmax = Math.min(x1, x2);
		ymin = Math.min(y1, y2);
		ymax = Math.min(y1, y2);
	}
	
	public boolean contains(Point p) {
		return p.x >= xmin && p.x <= xmax && p.y >= ymin && p.y <= ymax;
	}

}

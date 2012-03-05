import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;


public class BezierIntersection {
	
	public final BezierCurve p, q;
	public final BezierPoint pIntersect, qIntersect;
	
	private BezierIntersection(BezierCurve p, BezierCurve q, BezierPoint pIntersect, BezierPoint qIntersect) {
		this.p = p;
		this.q = q;
		this.pIntersect = pIntersect;
		this.qIntersect= qIntersect;
	}
	
	public static BezierIntersection createSelfIntersection(BezierCurve c) {
		TreeSet<BezierPoint> selfIntersects = new TreeSet<BezierPoint>(new Comparator<BezierPoint>() {
			@Override
			public int compare(BezierPoint o1, BezierPoint o2) {
				return o1.t > o2.t ? 1 : -1;
			}
		});
		selfIntersects.addAll(c.selfIntersections());
		if (selfIntersects.isEmpty()) {
			return null;
		}
		return new BezierIntersection(c, c, selfIntersects.first(), selfIntersects.last());
	}
	
	public static Set<BezierIntersection> createIntersections(BezierCurve p, BezierCurve q) {
		Set<BezierIntersection> intersections = new HashSet<BezierIntersection>();
		
		Set<BezierPoint> pIntersects = p.intersections(q);
		Set<BezierPoint> qIntersects = q.intersections(p);
		for (BezierPoint pIntersect : pIntersects) {
			BezierPoint qIntersect = null;
			for (BezierPoint test : qIntersects) {
				if (pIntersect.distance(test) < 1.0) {
					qIntersect = test;
					break;
				}
			}
			intersections.add(new BezierIntersection(p, q, pIntersect, qIntersect));
		}
		
		return intersections;
	}
	
	@Override
	public String toString() {
		return "[" + pIntersect + "; " + qIntersect + "]";
	}

}

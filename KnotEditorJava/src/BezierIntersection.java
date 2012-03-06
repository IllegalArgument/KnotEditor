import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;


public class BezierIntersection {
	
	public static final double TOLERABILITY_FACTOR = 0.01 / BezierCurve.EPSILON;
	
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
		for (final BezierPoint pIntersect : pIntersects) {
			if (qIntersects.isEmpty()) {
				continue;
			}
			TreeSet<BezierPoint> sorted = new TreeSet<BezierPoint>(new Comparator<BezierPoint>() {
				@Override
				public int compare(BezierPoint o1, BezierPoint o2) {
					return pIntersect.distance(o1) - pIntersect.distance(o2) > 0.0 ? 1 : -1;
				}
			});
			sorted.addAll(qIntersects);
			BezierPoint qIntersect = sorted.first();
			Bounds pBounds = p.getControlBounds();
			Bounds qBounds = q.getControlBounds();
			double maxDistance = Math.max(Math.max(pBounds.width(), qBounds.width()), Math.max(pBounds.height(), qBounds.height())) / TOLERABILITY_FACTOR;
			if (pIntersect.distance(qIntersect) > maxDistance) {
				continue;
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

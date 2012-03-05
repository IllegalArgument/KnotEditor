
public class BezierPoint extends Point {
	
	public final BezierCurve c;
	public final double t;

	public BezierPoint(BezierCurve c, double t) {
		super(c.pointAt(t));
		this.c = c;
		this.t = t;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(t);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BezierPoint other = (BezierPoint) obj;
		if (Double.doubleToLongBits(t) != Double.doubleToLongBits(other.t))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "t = " + t + " => " + super.toString();
	}

}

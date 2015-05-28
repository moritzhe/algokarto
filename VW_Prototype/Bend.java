import java.awt.Color;

public class Bend extends Line {
	private boolean isPositive;
	private double area;

	public Bend(Point p1, Point p2, boolean pos) {
		super();
		add(p1);
		add(p2);
		isPositive = pos;
		if (pos) {
			setColor(Color.BLUE);
		} else {
			setColor(Color.RED);
		}
	}

	public Bend(Line line) {
		super();
		this.points = line.points;
		isPositive = false;
		update();
	}

	public boolean isPositive() {
		return isPositive;
	}

	public double area() {
		if (area >= 0) {
			return area;
		}
		area = 0;
		Point prev = points.get(points.size() - 1);
		for (Point cur : points) {
			area += (prev.x + cur.x) * (prev.y - cur.y);
			prev = cur;
		}
		area = Math.abs(area / 2.0);
		return area;
	}

	public double getCircumference() {
		return getLength() + getBaseLength();
	}

	public double getCompactness() {
		return 4 * Math.PI * area() / (getCircumference() * getCircumference());
	}

	public double getAdjustedSize() {
		return area() * .75 / getCompactness();
	}

	@Override
	public void update() {
		super.update();
		area = -1;
		area = area();
	}

	public double getAvgCurve() {
		double curve = 0;
		for (int i = 1; i < points.size() - 1; i++) {
			curve += getTheta(points.get(i - 1), points.get(i),
					points.get(i + 1));
		}
		return Math.abs(curve / getLength());
	}

	/**
	 * As calculated in the paper. Unfortunately, no threshold was provided.
	 * 
	 * @param otherBend
	 * @return
	 */
	public double similarityTo(Bend otherBend) {
		double sizeNorm = (otherBend.getAdjustedSize() + getAdjustedSize()) / 2.0;
		double cmpNorm = (otherBend.getCompactness() + getCompactness()) / 2.0;
		double baseNorm = (otherBend.getBaseLength() + getBaseLength()) / 2.0;
		double sizeSquare = Math.pow(
				((otherBend.getAdjustedSize() - getAdjustedSize()) / sizeNorm),
				2);
		double cmpSquare = Math
				.pow(((otherBend.getCompactness() - getCompactness()) / cmpNorm),
						2);
		double baseSquare = Math.pow(
				((otherBend.getBaseLength() - getBaseLength()) / baseNorm), 2);
		return Math.sqrt(sizeSquare + cmpSquare + baseSquare);
	}

	/**
	 * 
	 * @param p1
	 *            anfangspunkt
	 * @param p3
	 *            endpunkt
	 * @return
	 */
	private double getTheta(Point p1, Point p2, Point p3) {
		return Math.acos(//
				((p2.x - p1.x) * (p3.x - p2.x) + (p2.y - p1.y) * (p3.y - p2.y))//
						/ (p1.distance(p2) * p2.distance(p3)));
	}

}

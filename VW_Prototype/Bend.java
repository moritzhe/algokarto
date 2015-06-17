import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bend extends Line {
	private boolean isPositive;
	private double area = -1;
	private Line parentLine;
	public static boolean ignoreChecks = false;

	public Bend(Point p1, Point p2, boolean pos, Line parentLine) {
		super();
		add(p1);
		add(p2);
		isPositive = pos;
		if (pos) {
			setColor(Color.BLUE);
		} else {
			setColor(Color.RED);
		}
		this.parentLine = parentLine;
		assert(validatePointLineRelationShip());
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
		if (points.size() < 3)
			return 0;
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

	/**
	 * Curve/length
	 * 
	 * @return Curve/length (in radians per unit length)
	 */
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
		double cmpSquare = Math.pow(
				((otherBend.getCompactness() - getCompactness()) / cmpNorm), 2);
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
	 * @return zwischen 0 und pi
	 */
	public static double getTheta(Point p1, Point p2, Point p3) {
		return Math.acos(//
				((p2.x - p1.x) * (p3.x - p2.x) + (p2.y - p1.y) * (p3.y - p2.y))//
						/ (p1.distance(p2) * p2.distance(p3)));
	}

	public boolean eliminate() {
		return eliminate(null);
	}

	public boolean eliminate(MapData mapToCheck) {

		if (mapToCheck != null) {
			// Schneiden Lines die BaseLine
			for (Line line : mapToCheck.lines) {
				if (lineIntersectsBaseLine(line))
					return false;
			}
			for (Point p : mapToCheck.pois) {
				if (isPointInBendArea(p))
					return false;
			}
		}

		while (points.size() > 2) {
			points.get(1).loesch();
		}

		return true;
	}

	public boolean exaggerate() {
		// if bend is empty, do nothing
		if (points.size() == 0)
			return false;

		// Original Points to restore
		List<Point> originalPoints = new ArrayList<Point>();
		for (Point p : parentLine.points) {
			p.beginOfTransaction();
			originalPoints.add(p);
		}

		final double exaggerationFactor = 0.5;
		final double baseLineLength = getBaseLength();

		// calculate midpoint of baseline (anchor point)
		Point firstPoint = points.get(0);
		Point lastPoint = points.get(points.size() - 1);
		final double x = (firstPoint.x + lastPoint.x) / 2;
		final double y = (firstPoint.y + lastPoint.y) / 2;
		Point anchorPoint = new Point(x, y);

		// don't move endpoints
		for (int i = 1; i < parentLine.size() - 1; i++) {
			Point p = parentLine.points.get(i);
			final double distanceToAnchorPoint = p.distance(anchorPoint);

			final double dx = (p.x - anchorPoint.x) / distanceToAnchorPoint;
			final double dy = (p.y - anchorPoint.y) / distanceToAnchorPoint;

			final double scaledDistance = distanceToAnchorPoint
					/ baseLineLength;
			final double scalingFactor = gauss(scaledDistance, 0, 1);
			final double absoluteExeggerationValue = scalingFactor
					* baseLineLength * exaggerationFactor;

			final double xOfsset = dx * absoluteExeggerationValue;
			final double yOffset = dy * absoluteExeggerationValue;

			p.setPosition(p.x + xOfsset, p.y + yOffset);
		}

		if (!parentLine.stillTopologicallyCorrect()) {
			for (Point p : originalPoints) {
				p.rollbackTransaction();
			}
			return false;
		}
		
		for (Point p : originalPoints) {
			p.commitTransaction();
		}

		return true;
	}

	/**
	 * Combines this bend with next two bends
	 * 
	 * @param bendB
	 *            Next
	 * @param bendC
	 *            Next next
	 */

	public boolean combine(Bend bendB, Bend bendC) {
		String str = parentLine.output();
		
		final double movementFactorMultiplier = 0.5;
		
		assert(validatePointLineRelationShip());
		assert(bendB.validatePointLineRelationShip());
		assert(bendC.validatePointLineRelationShip());
		
		// Bends muessen benachbart sein
		if (points.get(points.size() - 1) != bendB.points.get(1)) {
			System.out.println("Nicht benachbart: B");
			return false;
		}
		if (bendB.points.get(bendB.points.size() - 1) != bendC.points.get(1)) {
			System.out.println("Nicht benachbart: C");
			return false;
		}

		// Original Points to restore
		List<Point> originalPoints = new ArrayList<Point>();
		for (Point p : parentLine.points) {
			p.beginOfTransaction();
			originalPoints.add(p);
		}
		
		// Vorgeschlagener Wert vom Paper
		final double combinedPeakFactor = 1.2;

		// Peaks finden
		Point A = getPeak();
		Point B = bendB.getPeak();
		Point C = bendC.getPeak();

		// Jetzt ueberfluessig gewordene Punkte eliminieren
		setNewEndingPoint(A);
		bendB.removeAllPointsExceptFor(A, B, C);
		bendC.setNewStartingPoint(C);
		Point D = new Point((A.x + C.x) / 2, (A.y + C.y) / 2);

		double BDx = D.x - B.x;
		double BDy = D.y - B.y;
		BDx *= combinedPeakFactor;
		BDy *= combinedPeakFactor;

		// B wird neuer Peak
		B.setPosition(B.x + BDx, B.y + BDy);
		Point Ds = B;

		double ADsx = Ds.x - A.x;
		double ADsy = Ds.y - A.y;

		double CDsx = Ds.x - C.x;
		double CDsy = Ds.y - C.y;

		// Bewegung pro Punkt
		Map<Point, Double> movementFactors = new HashMap<Point, Double>();
		
		//Regression Check: getLength ist korrekt
		if (bendC.getLength() != bendC.getDistanceBetween(
				bendC.points.get(0), bendC.points.get(bendC.points.size()-1))){
			System.out.println("Bend Length:" + bendC.getLength());
			System.out.println("Dist Between: " + bendC.getDistanceBetween(
				bendC.points.get(0), bendC.points.get(bendC.points.size()-1)));
			assert(false);
		}
		
		for (Point p : points) {
			Point firstPoint = points.get(0);
			double distanceToFirst = getDistanceBetween(firstPoint, p);
			double factor = distanceToFirst / getLength();
			movementFactors.put(p, factor * movementFactorMultiplier);
			// System.out.println(factor);
		}
		assert (movementFactors.get(A) == 1.0 * movementFactorMultiplier);
		assert (movementFactors.get(points.get(0)) == 0.0);

		// System.out.println("-");

		for (Point p : bendC.points) {
			Point lastPoint = bendC.points.get(bendC.points.size() - 1);
			double distanceToLast = bendC.getDistanceBetween(lastPoint, p);
			double factor = distanceToLast / bendC.getLength();
			movementFactors.put(p, factor * movementFactorMultiplier);
			// System.out.println(factor);
		}
		assert (movementFactors.get(C) == 1.0 * movementFactorMultiplier);
		assert (movementFactors.get(bendC.points.get(bendC.points.size() - 1)) == 0.0);

		// DebugCode
		// Point _A = points.get(2);
		// Point _F = points.get(1);
		// Point _E = points.get(0);
		// double testFactor = _F.distance(_E) / (_A.distance(_F) +
		// _F.distance(_E));
		// System.out.println(testFactor);
		// System.out.println(":" + (_F.x + ADsx * testFactor) + "," +(_F.y +
		// ADsy * testFactor));
		//
		// //DebugCode
		// _A = bendC.points.get(0);
		// _F = bendC.points.get(1);
		// _E = bendC.points.get(2);
		// testFactor = _F.distance(_E) / (_A.distance(_F) + _F.distance(_E));
		// System.out.println(testFactor);
		//
		// System.out.println("___");
		// System.out.println(A.x);
		// System.out.println(A.y);
		//
		// System.out.println(Ds.x);
		// System.out.println(Ds.y);
		//
		// System.out.println(ADsx);
		// System.out.println(ADsy);

		A.loesch();
		C.loesch();

		// Punkte an neue Positionen bewegen
		for (Point p : points) {
			double movementFactor = movementFactors.get(p).doubleValue();
			p.setPosition(p.x + ADsx * movementFactor, p.y + ADsy
					* movementFactor);
		}

		for (Point p : bendC.points) {
			double movementFactor = movementFactors.get(p).doubleValue();
			p.setPosition(p.x + CDsx * movementFactor, p.y + CDsy
					* movementFactor);
		}

		// Ueberpruefen, ob die neue Bend valide ist
		if (!ignoreChecks && !parentLine.stillTopologicallyCorrect()) {
			// Revert changes
			for (Point p : originalPoints) {
				p.rollbackTransaction();
			}
			return false;
		} else {
			// Save changes
			for (Point p : originalPoints) {
				p.commitTransaction();
			}
			System.out.println(str);
			return true;
		}
	}

	public Point getPeak() {
		Point peak = points.get(0);
		double peakDistance = 0;
		for (Point p : points) {
			final double pDistance = p.distance(points.get(0))
					+ p.distance(points.get(points.size() - 1));
			if (pDistance > peakDistance) {
				peak = p;
				peakDistance = pDistance;
			}
		}
		return peak;
	}

	public void setNewEndingPoint(Point p) {
		int newEndIndex = points.indexOf(p);
		if (newEndIndex == -1) {
			System.out.println("Kein Linienpunkt!");
			return;
		}
		while (points.size() > newEndIndex + 1) {
			assert(points.get(newEndIndex + 1).containedByLine(this));
			points.get(newEndIndex + 1).loesch();
		}
	}

	public void setNewStartingPoint(Point p) {
		int newStartIndex = points.indexOf(p);
		if (newStartIndex == -1) {
			System.out.println("Kein Linienpunkt!");
			return;
		}
		while (points.get(0) != p) {
			assert(points.get(0).containedByLine(this));
			points.get(0).loesch();
		}
	}

	public void removeAllPointsExceptFor(Point p1, Point p2, Point p3) {
		List<Point> remove = new ArrayList<Point>();
		for (Point p : points) {
			if (p != p1 && p != p2 && p != p3) {
				remove.add(p);
			}
		}
		for (Point p : remove) {
			p.loesch();
		}
	}

	static double gauss(double x, double my, double sigma2) {
		final double pi = Math.PI;
		return (1.0 / Math.sqrt(2 * pi * sigma2))
				* Math.exp(-(((x - my) * (x - my)) / (2 * sigma2)));
	}

	public boolean lineIntersectsBaseLine(Line line) {
		for (int i = 0; i < line.points.size() - 1; ++i) {
			if (line == parentLine) {
				if (line.points.get(i) == points.get(0)
						|| line.points.get(i + 1) == points.get(0)
						|| line.points.get(i) == points.get(points.size() - 1)
						|| line.points.get(i + 1) == points
								.get(points.size() - 1)) {
					continue;
				}
			}
			if (lineSegmentsIntersect(line.points.get(i),
					line.points.get(i + 1), points.get(0),
					points.get(points.size() - 1)))
				return true;
		}
		return false;
	}
	

}

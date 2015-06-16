import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Line implements GMLObject {

	protected List<Point> points;
	private double length = -1;
	private Path2D.Double path;
	private Color color = Color.BLACK;
	private Point start, end;
	private Line complement;
	private Set<Point> poisInBounds;
	private MapData map;
	boolean pathInvalid = false;

	private List<Point> totalPOIS;

	public Line() {
		points = new ArrayList<Point>();
		path = new Path2D.Double();
	}

	public Line(List<Point> points) {
		this();
		for (Point p : points) {
			add(p);
		}
	}

	public void add(Point p) {
		points.add(p);
		p.addToLine(this);
		if (points.size() == 1) {
			length = 0;
			if (!pathInvalid)
				path.moveTo(p.x, p.y);
		} else {
			length += p.distance(points.get(points.size() - 2));
			if (!pathInvalid)
				path.lineTo(p.x, p.y);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		drawWithColor(g, color);
	}

	@Override
	public void drawWithColor(Graphics2D g, Color color) {
		Color c = g.getColor();
		g.setColor(color);
		if (pathInvalid) {
			recalculatePath();
		}
		g.draw(path);
		g.setColor(c);
	}

	private void recalculatePath() {
		if (pathInvalid && size() >= 1) {
			path.moveTo(get(0).x, get(0).y);
			for (int i = 1; i < size(); i++) {
				path.lineTo(get(i).x, get(i).y);
			}
		}
		pathInvalid = false;
	}

	protected List<Bend> findBends() {
		List<Bend> bends = new ArrayList<Bend>();
		if (this.size() < 3) {
			bends.add(new Bend(this));
			return bends;
		}
		Bend bend = new Bend(this.get(0), this.get(1), isPositive(this.get(0),
				this.get(1), this.get(2)), this);
		for (int i = 2; i < this.size() - 1; i++) {
			boolean pos = isPositive(this.get(i - 1), this.get(i),
					this.get(i + 1));
			bend.add(this.get(i));
			if ((pos && !bend.isPositive()) || (!pos && bend.isPositive())) {
				bends.add(bend);
				bend = new Bend(this.get(i - 1), this.get(i), pos, this);
			}
		}
		// Der letzte Punkt is immer auf den gleichen Bend wie der vorletzte
		bend.add(this.get(this.size() - 1));
		bends.add(bend);
		return bends;
	}

	public Point get(int i) {
		return points.get(i);
	}

	public double getBaseLength() {
		// Baseline-Laenge
		return points.get(0).distance(points.get(points.size() - 1));
	}

	@Override
	public Rectangle getBounds() {
		if (pathInvalid)
			recalculatePath();
		return path.getBounds();
	}

	public double getDistanceBetween(Point a, Point b) {
		int aIdx = points.indexOf(a);
		int bIdx = points.indexOf(b);
		if (aIdx == -1 || bIdx == -1) {
			return -1;
		}
		if (aIdx > bIdx) {
			int tmp = bIdx;
			bIdx = aIdx;
			aIdx = tmp;
		}
		double dist = 0.0;
		for (int i = aIdx; i < bIdx; ++i) {
			dist += points.get(i).distance(points.get(i + 1));
		}
		return dist;
	}

	public double getLength() {
		if (length == -1) {
			length = 0;
			for (int i = 0; i < size() - 2; i++) {
				length += get(i).distance(get(i + 1));
			}
		}
		return length;
	}

	public boolean isPointInBendArea(Point p) {
		return isPointInBendArea(p, points);
	}

	public static boolean isPointInBendArea(Point p, List<Point> points) {

		int numPoints = points.size();
		int i = 0;
		int j = numPoints - 1;
		int numberOfIntersections = 0;
		for (; i < numPoints; i++) {
			Point pointI = points.get(i);
			Point pointJ = points.get(j);
			if (pointI.y < p.y && pointJ.y >= p.y || pointJ.y < p.y
					&& pointI.y >= p.y) {
				if (pointI.x + (p.y - pointI.y) / (pointJ.y - pointI.y)
						* (pointJ.x - pointI.x) < p.x) {
					numberOfIntersections++;
				}
			}
			j = i;
		}

		return (numberOfIntersections % 2) == 1;
	}

	/**
	 * 
	 * @param p1
	 *            anfangspunkt
	 * @param p3
	 *            endpunkt
	 * @return
	 */
	protected boolean isPositive(Point p1, Point p2, Point p3) {
		return (p2.x - p1.x) * (p3.y - p2.y) - (p2.y - p1.y) * (p3.x - p2.x) > 0;
	}

	/**
	 * True if elements of line are equal (doesn't check for reversed order)
	 */
	@Override
	public boolean equals(Object line) {
		if (!(line instanceof Line))
			return false;
		Line l2 = (Line) line;
		if (l2.size() != size())
			return false;
		for (int i = 0; i < size(); i++) {
			if (!get(i).equals(l2.get(i)))
				return false;
		}
		return true;
	}

	/**
	 * Checks whether this line intersects with the given line. If the lines are
	 * equal, checks if the given line has a self-intersection. <br>
	 * <br>
	 * If intersect only at end point, returns false.
	 * 
	 * @param line
	 * @return
	 */
	public boolean lineIntersects(Line line) {
		if (line.equals(this)) {
			return lineSelfIntersects();
		}
		// check middle segments
		for (int i = 0; i < line.points.size() - 1; ++i) {
			if (lineSegmentIntersects(line.points.get(i),
					line.points.get(i + 1))) {
				if (i == 0) {
					Point mid = line.get(0);
					if (mid.equals(start)) {
						if (this.neighborSegmentIntersect(line.points.get(1),
								mid, get(1)))
							return true;
					} else if (mid.equals(end)) {
						if (this.neighborSegmentIntersect(line.points.get(1),
								mid, get(size() - 2)))
							return true;
					} else
						return true;
				} else if (i == line.points.size() - 2) {
					Point mid = line.get(i + 1);
					if (mid.equals(start)) {
						if (this.neighborSegmentIntersect(line.points.get(i),
								mid, get(1)))
							return true;
					} else if (line.points.get(0).equals(end)) {
						if (this.neighborSegmentIntersect(line.points.get(i),
								mid, get(size() - 2)))
							return true;
					} else
						return true;
				} else
					return true;
			}
		}

		return false;
	}

	public boolean lineSegmentIntersects(Point a, Point b) {
		return lineSegmentIntersectsInRange(a, b, 0, size());
	}

	public boolean lineSegmentIntersectsInRange(Point a, Point b, int start,
			int length) {
		if (length <= 1) {
			return false;
		}
		for (int i = start; i < start + length - 1; ++i) {
			if (lineSegmentsIntersect(points.get(i), points.get(i + 1), a, b))
				return true;
		}
		return false;
	}

	/**
	 * True if line segments intersect
	 * 
	 * @param pointA1
	 * @param pointA2
	 * @param pointB1
	 * @param pointB2
	 * @return
	 */
	public static boolean lineSegmentsIntersect(Point pointA1, Point pointA2,
			Point pointB1, Point pointB2) {
		return Line2D.linesIntersect(pointA1.x, pointA1.y, pointA2.x,
				pointA2.y, pointB1.x, pointB1.y, pointB2.x, pointB2.y);
	}

	public boolean lineSelfIntersects() {
		for (int i = 0; i < points.size() - 2; ++i) {
			// does segment intersect with non-neighboring segments
			// that come after it?
			if (lineSegmentIntersectsInRange(points.get(i), points.get(i + 1),
					i + 2, size() - i - 2))
				return true;

			// now check whether it intersects with segment + 1:
			if (neighborSegmentIntersect(get(i), get(i + 1), get(i + 2))) {
				return true;
			}
		}
		return false;
	}

	public boolean neighborSegmentIntersect(Point p1, Point mid, Point p2) {
		double s1 = (mid.y - p1.y) / (mid.x - p1.x), s2 = (mid.y - p2.y)
				/ (mid.x - p2.x);
		// if same slope and same side of point e, segments overlap
		if (s1 == s2 && ( // same slope AND
				// both above/below and left/right
				(((mid.y > p1.y) == (mid.y > p2.y)) && ((mid.x > p1.x) == (mid.x > p2.x))) //
						// or identical y and same x direction
						|| (mid.y == p1.y && ((mid.x > p1.x) == (mid.x > p2.x))) //
				// or identical x and same y direction
				|| (mid.x == p1.x && ((mid.y > p1.y) == (mid.y > p2.y))) //
				))
			return true;
		return false;
	}

	public String output() {
		String str = "";
		for (int i = 0; i < points.size(); i++) {
			str += points.get(i) + " ";
		}
		return str.trim();
	}

	public void recordEndPoints() {
		start = new Point(get(0).x, get(0).y);
		end = new Point(get(size() - 1).x, get(size() - 1).y);
	}

	public void remove(Point p) {
		points.remove(p);
		p.loesch();
		update();
	}

	public void setColor(Color c) {
		color = c;
	}

	public void setComplement(Line comp) {
		complement = comp;
	}

	public void setInitContains(List<Point> pois) {
		totalPOIS = pois;
		poisInBounds = topoPOIs();
	}

	public void setMapData(MapData map) {
		this.map = map;
	}

	public int size() {
		return points.size();
	}

	/**
	 * Checks that line doesn't intersect with other lines, that points are on
	 * the same side of it as at the beginning, and that the start and end
	 * points haven't moved
	 * 
	 * @return
	 */
	public boolean stillTopologicallyCorrect() {
		for (Line line : map.lines) {
			if (lineIntersects(line)){
				System.out.println("Bad intersect");
				return false;
			}
		}
		if (!(topoPOIs().equals(poisInBounds) && get(0).equals(start)
				&& get(size() - 1).equals(end))){
			System.out.println("Pois: "+topoPOIs().equals(poisInBounds)+" presumably start and end are automatic");
		}
		return topoPOIs().equals(poisInBounds) && get(0).equals(start)
				&& get(size() - 1).equals(end);
	}

	private Set<Point> topoPOIs() {
		List<Point> list = new ArrayList<Point>();
		list.addAll(points);
		list.addAll(complement.points);
		Set<Point> poisInBounds = new HashSet<Point>();
		for (Point poi : totalPOIS) {
			if (isPointInBendArea(poi, list)) {
				poisInBounds.add(poi);
			}
		}
		return poisInBounds;
	}

	public String toString() {
		return points.toString();
	}

	public void update() {
		length = -1;
		// for (int i = 1; i < points.size(); i++) {
		// length += points.get(i - 1).distance(points.get(i));
		// }

		pathInvalid = true;
		// if (points.size() == 0)
		// return;
		// path.moveTo(points.get(0).x, points.get(0).y);
		// for (int i = 1; i < points.size(); i++) {
		// path.lineTo(points.get(i).x, points.get(i).y);
		// }
	}
}

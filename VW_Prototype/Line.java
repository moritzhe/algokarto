import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Line implements GMLObject {

	protected List<Point> points;
	private double length = -1;
	private Path2D.Double path;
	private Color color = Color.BLUE;
	private Point start, end;
	private Line complement;
	private Set<Point> poisInBounds;
	private MapData map;
	boolean pathInvalid = false;

	private List<Point> totalPOIS;

	protected List<Point> pointsBeforeTransaction;
	protected boolean inTransaction = false;
	protected boolean currentTransactionRolledBack = false;

	public boolean isInTransaction() {
		return inTransaction;
	}

	public Point getLastPoint() {
		return points.get(points.size() - 1);
	}

	public void beginOfTransaction() {
		assert (!inTransaction);
		inTransaction = true;
		currentTransactionRolledBack = false;
		pointsBeforeTransaction = new ArrayList<Point>(points);
		for (Point p : points) {
			p.beginOfTransaction();
			p.propagateBeginOfTransaction(this);
		}
	}

	/**
	 * Only makes sure has same points
	 * 
	 * @return
	 */
	public Line copy() {
		Line l2 = new Line();
		for (Point p : points) {
			l2.add(new Point(p.x, p.y));
		}
		return l2;
	}

	public void receiveBeginOfTransaction() {
		assert (!inTransaction);
		inTransaction = true;
		currentTransactionRolledBack = false;
		pointsBeforeTransaction = new ArrayList<Point>(points);
	}

	public void receiveRollbackTransaction() {
		assert (inTransaction);
		if (!currentTransactionRolledBack){
			points = new ArrayList<Point>(pointsBeforeTransaction);
			currentTransactionRolledBack = true;
		}
	}

	public void receiveCommitTransaction() {
		assert (inTransaction);
		inTransaction = false;
		currentTransactionRolledBack = false;
		pointsBeforeTransaction.clear();
		update();
	}

	public void commitTransaction() {
		assert (inTransaction);
		inTransaction = false;
		currentTransactionRolledBack = false;
		for (Point p : pointsBeforeTransaction) {
			p.commitTransaction();
			p.propagateCommitTransaction(this);
		}
		pointsBeforeTransaction.clear();
		update();
	}

	public void rollbackTransaction() {
		assert (inTransaction);
		currentTransactionRolledBack = true;
		points = new ArrayList<Point>(pointsBeforeTransaction);
		for (Point p : points) {
			p.rollbackTransaction();
			p.propagateRollbackTransaction(this);
		}
		commitTransaction();
	}

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

	public Line(Line l) {
		this();
		for (Point p : l.points) {
			add(p);
		}
	}

	public void add(Point p) {
		points.add(p);
		p.addToLine(this);
		if (points.size() == 1) {
			length = 0;
			path = new Path2D.Double();
			path.moveTo(p.x, p.y);
			pathInvalid = false;
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
		// pathInvalid = true;
		if (pathInvalid) {
			recalculatePath();
		}
		g.draw(path);
		g.setColor(c);
	}

	private void recalculatePath() {
		if (pathInvalid) {
			path = new Path2D.Double();
			if (size() > 0) {
				path.moveTo(get(0).x, get(0).y);
				for (int i = 1; i < size(); i++) {
					path.lineTo(get(i).x, get(i).y);
				}
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
			assert (bend.validatePointLineRelationShip());
			bend.add(this.get(i));
			if (!bend.validatePointLineRelationShip()) {
				// System.out.println("Punkt: "+this.get(i));
				// Line testLine = new Line();
				// testLine.add(this.get(i));
				// System.out.println(this.get(i).equals(this.get(i)));
				assert (false);
			}
			if ((pos && !bend.isPositive()) || (!pos && bend.isPositive())) {
				bends.add(bend);
				bend = new Bend(this.get(i - 1), this.get(i), pos, this);
			}
		}
		// Der letzte Punkt is immer auf den gleichen Bend wie der vorletzte
		bend.add(this.get(this.size() - 1));
		bends.add(bend);

		for (Bend bendToCheck : bends) {
			assert (bendToCheck.validatePointLineRelationShip());
		}

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
		// System.out.println("Point ["+aIdx+"] "+points.get(aIdx).x+","+points.get(aIdx).y+":");
		// System.out.println(0);
		for (int i = aIdx; i < bIdx; ++i) {
			// System.out.println("Point ["+(i+1)+"] "+points.get(i+1).x+","+points.get(i+1).y+":");
			dist += points.get(i).distance(points.get(i + 1));
			// System.out.println(dist);
		}
		return dist;
	}

	public double getLength() {
		if (length == -1) {
			length = 0;
			for (int i = 0; i < size() - 1; i++) {
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
	 * True if elements of line are same, in either same or reversed order
	 */
	@Override
	public boolean equals(Object line) {
		if (!(line instanceof Line))
			return false;
		Line l2 = (Line) line;
		if (l2.size() != size())
			return false;
		boolean match = true;
		for (int i = 0; i < size(); i++) {
			if (!get(i).equals(l2.get(i))) {
				match = false;
			}
		}
		if (match)
			return true;
		for (int i = size() - 1; i >= 0; i--) {
			if (!get(i).equals(l2.get(i))) {
				return false;
			}
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

		if (size() == 2) {
			if (line.size() != 2)
				return line.lineIntersects(this);
			else {
				// if intersect
				if (lineSegmentsIntersect(start, end, line.start, line.end)) {
					// intersection is okay if exactly at start OR exactly at
					// end
					if (lineSegmentGoodEndNeighbors(line.start, line.end,
							start, end, true)
							|| lineSegmentGoodEndNeighbors(line.start,
									line.end, end, start, true))
						return false;
					// else, bad
					return true;
				} else
					return false;
			}
		}

		for (int i = 0; i < line.size() - 1; ++i) {

			// check against our middle segments
			if (lineSegmentIntersectsInRange(line.get(i), line.get(i + 1), 1,
					size() - 2)) {
				// System.out.println("Reg intersect: Other: " + line.get(i)
				// + " + " + line.get(i + 1));
				return true;
			}

			// if line segment is also in middle, not allowed to intersect at
			// all
			if (i != 0 && i != line.size() - 2) {
				if (lineSegmentsIntersect(line.get(i), line.get(i + 1), start,
						get(1)) || //
						lineSegmentsIntersect(line.get(i), line.get(i + 1),
								end, get(size() - 2)))
					return true;
				continue;
			}

			Point lineI = line.get(i), lineI1 = line.get(i + 1);
			// if there is some sort of extreme intersection
			if (lineSegmentsIntersect(lineI, lineI1, start, get(1))
					|| lineSegmentsIntersect(lineI, lineI1, end,
							get(size() - 2))) {

				// line extremes depend on i, so check both cases
				if ((i == 0 && lineSegmentBadIntersectAtExtreme(lineI, lineI1,
						line.size() == 2))
						|| (i == size() - 2 && lineSegmentBadIntersectAtExtreme(
								lineI1, lineI, line.size() == 2))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * True if segments intersect anywhere other than extreme points (either
	 * start or end)<br>
	 * <br>
	 * It assumes this line isn't degenerate
	 * 
	 * @param lineExtreme
	 * @param lineOther
	 * @param degenerateLine
	 *            True if the segment we're checking against is the entire line
	 * @return
	 */
	public boolean lineSegmentBadIntersectAtExtreme(Point lineExtreme,
			Point lineOther, boolean degenerateLine) {

		// check whether line segment intersects with our start
		Point extreme = start, ext2 = get(1);
		if (lineSegmentsIntersect(lineExtreme, lineOther, extreme, ext2)) {

			// still okay if they're good neighbors, but if not...
			if (!this.lineSegmentGoodEndNeighbors(lineExtreme, lineOther,
					extreme, ext2, degenerateLine)) {
				return true;
			}
		}

		// check whether line segment intersects with our end
		extreme = end;
		ext2 = get(size() - 2);
		if (lineSegmentsIntersect(lineExtreme, lineOther, extreme, ext2)) {
			// still okay if they're good neighbors, but if not...
			if (!this.lineSegmentGoodEndNeighbors(lineExtreme, lineOther,
					extreme, ext2, degenerateLine)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * True if the two segments intersect exactly at the endpoints <br>
	 * <br>
	 * If they intersect elsewhere or do not intersect at all, false
	 * 
	 * @param lineExtreme
	 * @param lineOther
	 * @param extreme
	 * @param ext2
	 * @param degenerate
	 * @return
	 */
	private boolean lineSegmentGoodEndNeighbors(Point lineExtreme,
			Point lineOther, Point extreme, Point ext2, boolean degenerate) {
		// if intersect at endpoint
		if (lineExtreme.equals(extreme)) {
			// and only at endpoint
			if (!this.neighborSegmentIntersect(lineOther, extreme, ext2)) {
				return true;
			}
		}
		// or other endpoint if segment is whole line
		else if (degenerate && lineOther.equals(extreme)) {
			// but only that endpoint
			if (!this.neighborSegmentIntersect(lineExtreme, extreme, ext2)) {
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
		// Note: if getting a null pointer exception here, try calling
		// "recordEndPoints()" of the original line(s) first
		return Line2D.linesIntersect(pointA1.x, pointA1.y, pointA2.x,
				pointA2.y, pointB1.x, pointB1.y, pointB2.x, pointB2.y);
	}

	public boolean lineSelfIntersects() {

		// small lines can't intersect themselves
		if (size() <= 2)
			return false;

		// special case only two segments
		// just make sure they're well-formed
		if (size() == 3) {
			if (neighborSegmentIntersect(get(0), get(1), get(2))) {
				return true;
			} else
				return false;
		}

		// check if start segment intersects with end segment
		if (lineSegmentsIntersect(start, get(1), end, get(size() - 2))) {
			// still okay if they're good neighbors, but if not...
			if (!this.lineSegmentGoodEndNeighbors(start, get(1), end,
					get(size() - 2), false)) {
				return true;
			}
		}

		// check other segments
		for (int i = 0; i < points.size() - 2; ++i) {
			// does segment intersect with non-neighboring segments
			// that come after it (except start with end?)
			if (lineSegmentIntersectsInRange(points.get(i), points.get(i + 1),
					i + 2, Math.min(size() - i - 2, size() - 3)))
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

	public void remove(Point p, boolean noCallback) {
		points.remove(p);
		if (!noCallback)
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
			if (lineIntersects(line)) {
				// System.out.println("Bad intersect size" + size() + ", "
				// + line.size() + ": " + this.output() + "\n"
				// + line.output());
				return false;
			}
		}
		// if (!(topoPOIs().equals(poisInBounds) && get(0).equals(start) && get(
		// size() - 1).equals(end))) {
		// System.out.println("Pois: " + topoPOIs().equals(poisInBounds)
		// + " presumably start and end are automatic");
		// }
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

	public boolean validatePointLineRelationShip() {
		for (Point p : points) {
			if (!p.containedByLine(this))
				return false;
		}
		return true;
	}
}

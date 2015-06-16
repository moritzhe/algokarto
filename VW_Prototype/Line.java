import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class Line implements GMLObject {
	protected List<Point> points;
	private double length = -1;
	private Path2D.Double path;
	private Color color = Color.BLACK;
	private Point start, end;
	private Line complement;

	public Line() {
		points = new ArrayList<Point>();
		path = new Path2D.Double();
	}
	
	public Line(List<Point> points) {
		this();
		for (Point p: points){
			add (p);
		}
	}

	public void setColor(Color c) {
		color = c;
	}

	public void add(Point p) {
		points.add(p);
		p.addToLine(this);
		if (points.size() == 1) {
			length = 0;
			path.moveTo(p.x, p.y);
		} else {
			length += p.distance(points.get(points.size() - 2));
			path.lineTo(p.x, p.y);
		}
	}

	public int size() {
		return points.size();
	}

	public double getLength() {
		return length;
	}

	public Point get(int i) {
		return points.get(i);
	}

	public void remove(Point p) {
		points.remove(p);
		update();
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

	public void update() {
		length = 0;
		for (int i = 1; i < points.size(); i++) {
			length += points.get(i - 1).distance(points.get(i));
		}

		path = new Path2D.Double();
		if (points.size() == 0)
			return;
		path.moveTo(points.get(0).x, points.get(0).y);
		for (int i = 1; i < points.size(); i++) {
			path.lineTo(points.get(i).x, points.get(i).y);
		}
	}

	public String toString() {
		return points.toString();
	}

	public double getBaseLength() {
		// Baseline-Laenge
		return points.get(0).distance(points.get(points.size() - 1));
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

	@Override
	public Rectangle getBounds() {
		return path.getBounds();
	}

	@Override
	public void draw(Graphics2D g) {
		drawWithColor(g, color);
	}

	@Override
	public void drawWithColor(Graphics2D g, Color color) {
		Color c = g.getColor();
		g.setColor(color);
		g.draw(path);
		g.setColor(c);
	}

	public String output() {
		String str = "";
		for (int i = 0; i < points.size(); i++) {
			str += points.get(i) + " ";
		}
		return str.trim();
	}

	public List<Point> getPointsInLowerBounds() {
		List<Point> bp = new ArrayList<Point>();
		if (size() <= 2)
			return bp;
		Rectangle r = getBounds();
		Point start = points.get(0), end = points.get(size() - 1);

		// consider start point extended beyond bounding box
		List<Point> options = new ArrayList<Point>();
		options.add(new Point(r.x - 10, start.y));
		options.add(new Point(start.x, r.y - 10));
		options.add(new Point(r.x + r.width + 10, start.y));
		options.add(new Point(start.x, r.y + r.height + 10));
		Point startOut = null;

		for (int i = 0; i < 4; i++) {
			boolean val = lineSegmentIntersectsInRange(start, options.get(i),
					1, size() - 1);
			if (!val) {
				startOut = options.get(i);
				break;
			}
		}

		if (startOut == null) {
			// TODO: fix this ugly case where the line is somehow looped around
			// the start point.
		}

		// now extend the end point beyond the bounding box
		options = new ArrayList<Point>();
		options.add(new Point(r.x - 10, end.y));
		options.add(new Point(end.x, r.y - 10));
		options.add(new Point(r.x + r.width + 10, end.y));
		options.add(new Point(end.x, r.y + r.height + 10));
		Point endOut = null;

		for (int i = 0; i < 4; i++) {
			boolean val = lineSegmentIntersectsInRange(end, options.get(i), 0,
					size() - 1);
			if (!val) {
				endOut = options.get(i);
				break;
			}
		}

		if (endOut == null) {
			// TODO: fix this ugly case where the line is somehow looped around
			// the end point.
		}

		Line extension = new Line();
		for (Point p : points) {
			extension.add(p);
		}
		extension.add(endOut);
		Point cur = endOut;

		// while you still need to go around a corner for it to turn out well
		while (!(startOut.x == cur.x && (startOut.x < r.x || startOut.x > r.x
				+ r.width))
				|| (startOut.y == cur.y && (startOut.y < r.y || startOut.y > r.y
						+ r.height))) {
			// if at extreme x, move to top or bottom corner of that side
			// closer to target
			if (cur.x < r.x || cur.x > r.x + r.width) {
				if (startOut.y < r.y + r.height / 2.0) {
					Point p = new Point(cur.x, r.y - 10);
					if (!p.equals(cur))
						extension.add(p);
				} else {
					Point p = new Point(cur.x, r.y + r.height + 10);
					if (!p.equals(cur))
						extension.add(p);
				}
			}

			// still need a corner?
			if ((startOut.x == cur.x && (startOut.x < r.x || startOut.x > r.x
					+ r.width))
					|| (startOut.y == cur.y && (startOut.y < r.y || startOut.y > r.y
							+ r.height))) {
				break;
			}

			// if at extreme y, move to left or right corner of that side
			// closer to target
			if (cur.y < r.y || cur.y > r.y + r.height) {
				if (startOut.x < r.x + r.width / 2.0) {
					Point p = new Point(r.x - 10, cur.y);
					if (!p.equals(cur))
						extension.add(p);
				} else {
					Point p = new Point(r.x + r.width + 10, cur.y);
					if (!p.equals(cur))
						extension.add(p);
				}
			}
		}
		extension.add(startOut);

		// In effect, add pluses outside bounding box to
		// produce extension of line to ring with no
		// self-intersections
		// __________________
		// | . . * . . . *
		// | . * . * * * . *
		// +| * . . . . . . *
		// +| . . . . . * *
		// +| --------- + ----
		// ++++++++++++++

		// TODO: figure out which points are in that ring
		// return extension, too?

		return null;
	}

	/**
	 * True if line segments intersect, but false if segments are equal or do
	 * not intersect
	 * 
	 * @param pointA1
	 * @param pointA2
	 * @param pointB1
	 * @param pointB2
	 * @return
	 */
	public static boolean lineSegmentsIntersect(Point pointA1, Point pointA2,
			Point pointB1, Point pointB2) {
		// if segments are actually equal
		if ((pointA1.equals(pointB1) || pointA2.equals(pointB2))
				|| (pointA1.equals(pointB2) || pointA2.equals(pointB1)))
			return false;
		// else
		return Line2D.linesIntersect(pointA1.x, pointA1.y, pointA2.x,
				pointA2.y, pointB1.x, pointB1.y, pointB2.x, pointB2.y);
	}

	public boolean lineSegmentIntersects(Point a, Point b) {
		return lineSegmentIntersectsInRange(a, b, 0, size());
	}

	public boolean lineSegmentIntersectsInRange(Point a, Point b, int start,
			int length) {
		for (int i = start; i < start + length - 1; ++i) {
			if (lineSegmentsIntersect(points.get(i), points.get(i + 1), a, b))
				return true;
		}
		return false;
	}

	public boolean lineIntersects(Line line) {
		for (int i = 0; i < line.points.size() - 1; ++i) {
			if (lineSegmentIntersects(line.points.get(i),
					line.points.get(i + 1)))
				return true;
		}
		return false;
	}

	public void recordEndPoints() {
		start = new Point(get(0).x, get(0).y);
		end = new Point(get(size() - 1).x, get(size() - 1).y);
	}

	public void setComplement(Line comp) {
		complement = comp;
	}
}

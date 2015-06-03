import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class Line implements GMLObject{
	protected List<Point> points;
	private double length = -1;
	private Path2D.Double path;
	private Color color = Color.BLACK;

	public Line() {
		points = new ArrayList<Point>();
		path = new Path2D.Double();
	}
	
	public void setColor (Color c){
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
		Bend bend = new Bend(this.get(0), this.get(1), isPositive(
				this.get(0), this.get(1), this.get(2)));
		for (int i = 2; i < this.size() - 1; i++) {
			boolean pos = isPositive(this.get(i - 1), this.get(i),
					this.get(i + 1));
			bend.add(this.get(i));
			if ((pos && !bend.isPositive()) || (!pos && bend.isPositive())) {
				bends.add(bend);
				bend = new Bend(this.get(i - 1), this.get(i), pos);
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
		if (points.size() == 0) return;
		path.moveTo(points.get(0).x, points.get(0).y);
		for (int i = 1; i < points.size(); i++) {
			path.lineTo(points.get(i).x, points.get(i).y);
		}
	}

	public void remove(int i) {
		remove(points.get(i));
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
		if (aIdx == -1 || bIdx == -1){
			return -1;
		}
		if (aIdx > bIdx){
			int tmp = bIdx;
			bIdx = aIdx;
			aIdx = tmp;
		}
		double dist = 0.0;
		for (int i = aIdx; i < bIdx; ++i){
			dist += points.get(i).distance(points.get(i+1));
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
}

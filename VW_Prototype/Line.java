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

	public void update() {
		length = 0;
		for (int i = 1; i < points.size(); i++) {
			length += points.get(i - 1).distance(points.get(i));
		}

		path = new Path2D.Double();
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

	@Override
	public Rectangle getBounds() {
		return path.getBounds();
	}

	@Override
	public void draw(Graphics2D g) {
		Color c = g.getColor();
		g.setColor(color);
		g.draw(path);
		g.setColor(c);
	}
}

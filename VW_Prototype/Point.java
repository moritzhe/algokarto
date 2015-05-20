import java.util.ArrayList;
import java.util.List;

public class Point {
	private List<Line> lines;
	double x;
	double y;

	public void loesch() {
		for (Line line : lines) {
			line.points.remove(this);
		}
	}

	public void addToLine(Line l) {
		lines.add(l);
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public Point(double x, double y) {
		super();
		this.x = x;
		this.y = y;
		lines = new ArrayList<Line>();
	}

	@Override
	public String toString() {
		return "Point [x=" + x + ", y=" + y + "]";
	}

}


public class Point {
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
	}
	double x;
	double y;
	@Override
	public String toString() {
		return "Point [x=" + x + ", y=" + y + "]";
	}
	
}

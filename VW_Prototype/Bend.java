public class Bend extends Line {
	private boolean isPositive;
	private double area;
	
	public Bend (Point p1, Point p2, boolean pos){
		super();
		add(p1);
		add(p2);
		isPositive = pos;
	}
	
	public Bend (Line line){
		super();
		this.points = line.points;
		isPositive = false;
	}
	
	public boolean isPositive(){
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
		area = Math.abs(area/2.0);
		return area;
	}

}

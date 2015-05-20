import java.util.ArrayList;


public class Line {
	ArrayList<Point> points;
	
	public Line(){
		points = new ArrayList<Point>();
	}
	
	public void add(Point p){
		points.add(p);
		p.addToLine(this);
	}
}

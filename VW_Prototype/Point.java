import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Point extends Point2D.Double{
	private List<Line> lines;

	public void loesch() {
		for (Line line : lines) {
			line.remove(this);
		}
	}

	public void addToLine(Line l) {
		lines.add(l);
	}

	public Point(double x, double y) {
		super(x,y);
		lines = new ArrayList<Line>();
	}
	
	public void setPostition(double x, double y){
		this.x = x;
		this.y = y;
		for (Line line : lines) {
			line.update();
		}
	}
	
	
	public GMLObject toGMLObject(){
		List<Point2D.Double> list = new ArrayList<Point2D.Double>();
		list.add(this);
		return new GMLObject(0,list,Color.RED);
	}

	@Override
	public String toString() {
		return "Point [x=" + getX() + ", y=" + getY() + "]";
	}

}

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;


public class Line {
	protected List<Point> points;
	private double length=-1;
	
	public Line(){
		points = new ArrayList<Point>();
	}
	
	public void add(Point p){
		points.add(p);
		p.addToLine(this);
		if (points.size()==1){
			length = 0;
		}else{
			length+=p.distance(points.get(points.size()-2));
		}
	}
	
	public int size(){
		return points.size();
	}
	
	public double getLength(){
		return length;
	}
	
	public Point get(int i){
		return points.get(i);
	}
	
	public void remove (Point p){
		points.remove(p);
	}
	
	public void remove (int i){
		points.remove(i);
	}
	
	public String toString(){
		return points.toString();
	}
	
	public double getBaseLength(){
		//Baseline-Laenge
		return points.get(0).distance(points.get(points.size()-1));
	}
}


public class AABB {
	double x1;
	double y1;
	double x2;
	double y2;
	
	public AABB(){
		x1 = Double.POSITIVE_INFINITY;
		y1 = Double.POSITIVE_INFINITY;
		x2 = Double.NEGATIVE_INFINITY;
		y2 = Double.NEGATIVE_INFINITY;
	}
	
	public void update(Point p){
		if (p.x < x1) x1 = p.x;
		if (p.y < y1) y1 = p.y;
		if (p.x > x2) x2 = p.x;
		if( p.y > y2) y2 = p.y;
	}
}

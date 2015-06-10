import java.util.ArrayList;


public class MapData {
	public MapData() {
		super();
		lines = new ArrayList<Line>();
		pois = new ArrayList<Point>();
	}
	ArrayList<Line> lines;
	ArrayList<Point> pois;
	public MapData(ArrayList<Line> lines, ArrayList<Point> pois) {
		this.lines = lines;
		this.pois = pois;
	}
	
	public int getSegments(){
		int segments = 0;
		for (Line line : lines) {
			segments+= line.size()-1;
		}
		return segments;
	}
}

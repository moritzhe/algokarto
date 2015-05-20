import java.awt.Color;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;

public class GMLObject {
	/** Liste von Punkte in verbundene Reihenfolge */
	private List<Point2D.Double> list;
	//protected double x, y;
	protected boolean isPoint;
	/** Id von Eingabe */
	int id;
	/** Graphische Repraesentation des GML Objekts */
	Path2D.Double path;
	Color color;

	public GMLObject(int id, List<Point2D.Double> list) {
		this(id, list, Color.BLACK);
	}

	public GMLObject(int id, List<Point2D.Double> list, Color color) {
		this.list = list;
		this.id = id;
		path = new Path2D.Double();
		path.moveTo(list.get(0).x, list.get(0).y);
		for (int i = 1; i < list.size(); i++) {
			path.lineTo(list.get(i).x, list.get(i).y);
		}
		isPoint = list.size() == 1;
		this.color = color;
	}

	public GMLObject(Bend bend) {
		// this.id = id;
		path = new Path2D.Double();
		path.moveTo(bend.points.get(0).x, bend.points.get(0).y);
		for (int i = 1; i < bend.points.size(); i++) {
			path.lineTo(bend.points.get(i).x, bend.points.get(i).y);
		}
		if (bend.isPositive()) {
			color = Color.RED;
		} else {
			color = Color.BLUE;
		}
	}

	@Override
	public String toString() {
		return id + " " + list;
	}
}
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Point extends Point2D.Double implements GMLObject {
	private List<Line> lines;
	private double transActPositionX;
	private double transActPositionY;
	private boolean inTransaction = false;
	
	@Override
	public boolean equals(Object o){
		if (o instanceof Point){
			//NaN-Punkte sind auch Equal
			return (this == o || super.equals(o));
		}
		
		return super.equals(o);
	}

	public void beginOfTransaction() {
		assert(!inTransaction);
		inTransaction = true;
		transActPositionX = x;
		transActPositionY = y;
	}

	public void rollbackTransaction() {
		setPosition(transActPositionX, transActPositionY);
	}

	public void commitTransaction() {

		inTransaction = false;
	}

	public void loesch() {
		
		for (Line line : lines) {
			//System.out.println("Line Size before: "+line.points.size());
			if(line.points.contains(this)){
				line.remove(this, true);
			}
			assert(!line.points.contains(this));
			//System.out.println("Line Size after: "+line.points.size());
		}
	
	}

	public void addToLine(Line l) {
		assert(!inTransaction);
		lines.add(l);
	}

	public Point(double x, double y) {
		super(x, y);
		lines = new ArrayList<Line>();
	}

	public void setPosition(double x, double y) {
		this.x = x;
		this.y = y;
		for (Line line : lines) {
			line.update();
		}
	}

	@Override
	public String toString() {
		return getX() + "," + getY();
		// return "Point [x=" + getX() + ", y=" + getY() + "]";
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle((int) x, (int) y, 0, 0);
	}

	@Override
	public void draw(Graphics2D g) {
		drawWithColor(g, Color.RED);
	}

	@Override
	public void drawWithColor(Graphics2D g, Color color) {
		Color c = g.getColor();
		g.setColor(color);
		AffineTransform at = g.getTransform();

		// half of final width/height in pixels
		double crossWidth = 3;

		// change so affine transform doesn't ruin
		double xDiff = crossWidth / at.getScaleX(), yDiff = crossWidth
				/ at.getScaleY();

		// draw
		Line2D.Double l = new Line2D.Double(x - xDiff, y, x + xDiff, y);
		g.draw(l);
		l = new Line2D.Double(x, y - yDiff, x, y + yDiff);
		g.draw(l);
		g.setColor(c);
	}
	
	public boolean containedByLine(Line line){
		return lines.contains(line);
	}
}

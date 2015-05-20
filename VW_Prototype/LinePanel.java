import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JPanel;


public class LinePanel extends JPanel {
	
	MapData map;
	
	public LinePanel(MapData map) {
		super();
		this.map = map;
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(Color.LIGHT_GRAY);
		g.clearRect(0, 0, getSize().width, getSize().height);
		g.fillRect(0, 0, getSize().width, getSize().height);
		g.setColor(new Color(255,31,0));
		
		AABB aabb = new AABB();
		
		for (Line line : map.lines){
			for (Point point: line.points){
				aabb.update(point);
			}
		}
		for (Point point: map.pois){
			aabb.update(point);
		}
		
		int bezel = 31;
		double width = getSize().width - 2 * bezel;
		double height = getSize().height - 2 * bezel;
		
		double data_width = aabb.x2 - aabb.x1;
		double data_height = aabb.y2 - aabb.y1;
		
		double factor = Math.min(width / data_width, height/data_height);
		
		for (Line line : map.lines){
			Point previous = line.points.get(0);
			for (Point point: line.points){
				int x = (int)(bezel + (point.x - aabb.x1) * factor);
				int y = (int)(height - bezel - (point.y - aabb.y1) * factor);
				
				int px = (int)(bezel + (previous.x - aabb.x1) * factor);
				int py = (int)(height - bezel - (previous.y - aabb.y1) * factor);
				
				g.fillRect(x-2, y-2, 4, 4);
				g.drawLine(px, py, x, y);
				previous = point;
			}
		}
		
		for (Point point: map.pois){
			g.setColor(new Color(0,0,231));
			int x = (int)(bezel + (point.x - aabb.x1) * factor);
			int y = (int)(height - bezel - (point.y - aabb.y1) * factor);
			
			
			g.fillRect(x-2, y-2, 4, 4);
		}
	}
	
}

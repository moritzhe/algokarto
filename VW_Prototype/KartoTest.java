import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

public class KartoTest implements KeyListener {
	JFrame frame;
	LinePanel pan;
	MapData map;

	public static void main(String[] args) {
		new KartoTest();
	}

	// einlesen
	public KartoTest() {
		map = new MapData();
		String data = "testdaten2";
		String lines_out = null;
		try {
			lines_out = new String(Files.readAllBytes(Paths.get(// data+"/
					"lines_out2.txt")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		lines_out = lines_out.replaceAll("\r", "")
				.replaceAll("\n[0-9]+\\:", "\n").replaceAll("^[0-9]+\\:", "");
		String[] lines_split = lines_out.split("\n");
		for (int i = 0; i < lines_split.length; ++i) {
			Line currentLine = new Line();
			lines_split[i] = lines_split[i]
					.replaceAll("<gml\\:LineString.*ts=\" \">", "")
					.replaceAll("</gml\\:coordinates.*$", "")
					.replaceAll(" $", "");
			String[] string_points = lines_split[i].split(" ");
			for (String string_point : string_points) {
				String[] single_point_split = string_point.split(",");
				currentLine.add(new Point(new Double(single_point_split[0])
						.doubleValue(), new Double(single_point_split[1])
						.doubleValue()));
			}
			map.lines.add(currentLine);
		}

		String points_out = null;
		try {
			points_out = new String(Files.readAllBytes(Paths.get(// data+"/
					"points_out2.txt")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		points_out = points_out.replaceAll("\r", "")
				.replaceAll("\n[0-9]+\\:", "\n").replaceAll("^[0-9]+\\:", "");
		String[] points_split = points_out.split("\n");
		for (int i = 0; i < points_split.length; ++i) {
			points_split[i] = points_split[i]
					.replaceAll("<gml\\:Point.*ts=\" \">", "")
					.replaceAll("</gml\\:coordinates.*$", "")
					.replaceAll(" $", "");
			String[] string_point_split = points_split[i].split(",");
			map.pois.add(new Point(new Double(string_point_split[0])
					.doubleValue(), new Double(string_point_split[1])
					.doubleValue()));
		}

		useOtherDisplay(map);

		// frame = new JFrame("Lines...");
		// pan = new LinePanel(map);
		// frame.add(pan);
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// frame.setBounds(200, 31, 1024, 768);
		// frame.addKeyListener(this);
		// frame.setVisible(true);
	}

	GMLPanel panel;

	private void useOtherDisplay(MapData map) {
		WM wm = new WM();
		List<Bend> bends = new ArrayList<Bend>();
		for (int i = 0; i < map.lines.size(); i++) {
			bends.addAll(wm.findBends(map.lines.get(i)));
		}

		List<GMLObject> gml = new ArrayList<GMLObject>();
		for (int i = 0; i < bends.size(); i++) {
			gml.add(new GMLObject(bends.get(i)));
		}
		
		for (int i=0; i<map.pois.size(); i++){
			gml.add(map.pois.get(i).toGMLObject());
		}

		panel = new GMLPanel();
		panel.list = gml;
		panel.calculateGMLBounds();
		JFrame frame = GMLPanel.showPanelInWindow(panel);
		frame.addKeyListener(this);
	}

	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void keyTyped(KeyEvent e) {
		for (int i = 0; i < 20; ++i)
			VW.Next(map);
		WM wm = new WM();
		List<Bend> bends = new ArrayList<Bend>();
		for (int i = 0; i < map.lines.size(); i++) {
			bends.addAll(wm.findBends(map.lines.get(i)));
		}

		List<GMLObject> gml = new ArrayList<GMLObject>();
		for (int i = 0; i < bends.size(); i++) {
			gml.add(new GMLObject(bends.get(i)));
		}

		panel.list = gml;
		panel.calculateGMLBounds();
		panel.repaint();
		// GMLPanel.setUpPanel(panel);

		//
		//
		// pan.repaint();
	}

}

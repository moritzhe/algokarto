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
	MapData map;
	GMLPanel panel;

	public static void main(String[] args) {
		KartoTest karto = new KartoTest();
		karto.display();
	}

	// einlesen und display
	public KartoTest() {

		String data = "";// "testdaten2/";
		map = readData(data + "lines_out2.txt", data + "points_out2.txt");

		// frame = new JFrame("Lines...");
		// pan = new LinePanel(map);
		// frame.add(pan);
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// frame.setBounds(200, 31, 1024, 768);
		// frame.addKeyListener(this);
		// frame.setVisible(true);
	}

	// einlesen
	public MapData readData(String lineFile, String pointsFile) {
		MapData map = new MapData();
		String lines_out = null;
		try {
			lines_out = new String(Files.readAllBytes(Paths.get(lineFile)));
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
			points_out = new String(Files.readAllBytes(Paths.get(pointsFile)));
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
			if (string_point_split.length >= 2) {
				map.pois.add(new Point(new Double(string_point_split[0])
						.doubleValue(), new Double(string_point_split[1])
						.doubleValue()));
			}
		}

		return map;
	}

	private List<GMLObject> updateBends() {
		WM wm = new WM();
		List<GMLObject> gml = new ArrayList<GMLObject>();
		for (int i = 0; i < map.lines.size(); i++) {
			gml.addAll(map.lines.get(i).findBends());
		}
		gml.addAll(map.pois);
		return gml;
	}

	private void display() {
		panel = new GMLPanel();
		panel.map = map;
		panel.setGMLObjects(updateBends());
		JFrame frame = GMLPanel.showPanelInWindow(panel);
		frame.addKeyListener(this);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == e.VK_DELETE) {
			panel.eliminateSelectedBend();
			panel.setGMLObjects(updateBends());
			panel.repaint();
		} else if (e.getKeyCode() == e.VK_E) {
			panel.exaggerateSelectedBend();
			panel.setGMLObjects(updateBends());
			panel.repaint();
		} else if (e.getKeyCode() == e.VK_C) {
			panel.combineSelectedBend();
			panel.setGMLObjects(updateBends());
			panel.repaint();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	private static double threshold = 50000;
	// Massachusetts shouldn't exaggerate bottom left bend
	private static double maxThreshold = 1.024E8;

	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getKeyChar() == ' ') {
			// for (int i = 0; i < 20; ++i)
			// VW.Next(map);
			WM.simplify(map, threshold);
			threshold = Math.min(threshold * 2, maxThreshold);
			panel.setGMLObjects(updateBends());
			panel.repaint();
		}
		if (e.getKeyChar() == 'v' || e.getKeyChar() == 'V') {
			for (int i = 0; i < 20; ++i)
				VW.Next(map,true);
			System.out.println("There are now "+map.getSegments()+" segments left");
			System.out.println();
			// WM.simplify(map, threshold);
			// threshold = Math.min(threshold * 2, maxThreshold);
			panel.setGMLObjects(updateBends());
			panel.repaint();
		}
	}

}

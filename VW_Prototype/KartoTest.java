import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

public class KartoTest implements KeyListener {
	JFrame frame;
	MapData map;
	GMLPanel panel;
	double vwAngleThreshold = Math.PI / 72;
	private String header = ":<gml:LineString srsName=\"EPSG:54004\" xmlns:gml=\"http://www.opengis.net/gml\"><gml:coordinates decimal=\".\" cs=\",\" ts=\" \">";
	private String footer = " </gml:coordinates></gml:LineString>";

	public static void main(String[] args) {

		// if args not valid, just testing
		boolean DEBUG = (args == null || args.length < 4);

		// if testing, make own args array
		if (DEBUG) {

			// 1: New Hampshire
			// 2: Massachusetts
			// 3: Paper combine test
			// 4: Iceland
			// 5: NH reduced to 300 lines using "simplify" (19:00, June 10)
			// 6: NH really weird combine
			int id = 4;
			args = new String[] { "600", "algokarto/lines_out" + id + ".txt",
					"algokarto/points_out" + id + ".txt", "results.txt" };
		}

		// parse args
		int maxEdgesToKeep = Integer.parseInt(args[0]);
		String lineInput = args[1];
		String pointInput = args[2];
		String output = args[3];

		// do work
		KartoTest karto = new KartoTest();
		karto.readData(lineInput, pointInput);
		if (!DEBUG)
			karto.simplify(maxEdgesToKeep);
		karto.writeFile(output);

		// if testing, show in panel
		if (DEBUG) {
			karto.display();
		}
	}

	private void writeFile(String output) {
		try {
			PrintWriter writer = new PrintWriter(output, "UTF-8");
			for (int i = 1; i <= map.lines.size(); i++) {
				writer.println(i + header + map.lines.get(i - 1).output()
						+ footer);
			}
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private void simplify(int maxEdgesToKeep) {
		int countWM = 0, countVW = 0;
		int segments = map.getSegments();
		double threshold = 0.001;
		while (segments > maxEdgesToKeep && threshold < maxThreshold) {
			WM.simplify(map, threshold);
			countWM += segments - map.getSegments();
			segments = map.getSegments();
			VW.removeAllSmall(map, vwAngleThreshold);
			countVW += segments - map.getSegments();
			segments = map.getSegments();
			threshold = Math.min(threshold * 2, maxThreshold);
		}

		double vwAtEnd = 0;
		while (segments > maxEdgesToKeep) {
			VW.Next(map, false);
			segments--;
			if (segments != map.getSegments()) {
				System.out.println("Unable to continue removing lines");
				break;
			}
			vwAtEnd++;
		}
		System.out.println(countWM + " " + countVW + " " + vwAtEnd);
	}

	// einlesen
	public void readData(String lineFile, String pointsFile) {
		map = new MapData();
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
		map.setFinal();
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
		System.out.println("Map is correct: "+map.isTopoCorrect());
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

	private static double threshold = 50000;// .001;
	// Massachusetts shouldn't exaggerate bottom left bend
	private static double maxThreshold = 1.024E8;
	private static double edges = -1;

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
				VW.Next(map, false);
			System.out.println("There are now " + map.getSegments()
					+ " segments left");
			System.out.println();
			// WM.simplify(map, threshold);
			// threshold = Math.min(threshold * 2, maxThreshold);
			panel.setGMLObjects(updateBends());
			panel.repaint();
		}
		if (e.getKeyChar() == 's' || e.getKeyChar() == 'S') {
			if (edges < 0) {
				edges = .75 * map.getSegments();
			}
			simplify((int) edges);
			edges *= .75;

			System.out.println("There are now " + map.getSegments()
					+ " segments left");
			System.out.println();
			panel.setGMLObjects(updateBends());
			panel.repaint();
		}
		System.out.println("Map is correct: "+map.isTopoCorrect());
	}

}

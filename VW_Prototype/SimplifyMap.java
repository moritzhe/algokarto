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

public class SimplifyMap implements KeyListener {
	JFrame frame;
	MapData mapWM, mapVW;
	GMLPanel panelWM, panelVW;
	double vwAngleThreshold = Math.PI / 72;
	private String header = ":<gml:LineString srsName=\"EPSG:54004\" xmlns:gml=\"http://www.opengis.net/gml\"><gml:coordinates decimal=\".\" cs=\",\" ts=\" \">";
	private String footer = " </gml:coordinates></gml:LineString>";
	public static final boolean DEBUG = true;

	public static void main(String[] args) {

		// if args not valid, just testing
		// boolean DEBUG = ();

		// if testing, make own args array
		if (args == null || args.length < 4) {
			System.out.println("Not enough arguments; choosing own defaults");

			// 1: New Hampshire
			// 2: Massachusetts
			// 3: Paper combine test
			// 4: Iceland
			// 5: NH reduced to 300 lines using "simplify" (19:00, June 10)
			// 6: NH really weird combine
			int id = 2;
			args = new String[] { "20000", "algokarto/lines_out" + id + ".txt",
					"algokarto/points_out" + id + ".txt", "results.txt" };
		}

		// parse args
		int maxEdgesToKeep = Integer.parseInt(args[0]);
		String lineInput = args[1];
		String pointInput = args[2];
		String output = args[3];

		// do work
		SimplifyMap karto = new SimplifyMap();
		karto.readData(lineInput, pointInput);
		karto.simplify(maxEdgesToKeep);

		// if testing, show in panel
		if (DEBUG) {
			karto.display();
		} else {
			karto.writeFile(output);
		}
	}

	private void writeFile(String output) {
		try {
			PrintWriter writer = new PrintWriter(output, "UTF-8");
			for (int i = 1; i <= mapWM.lines.size(); i++) {
				writer.println(i + header + mapWM.lines.get(i - 1).output()
						+ footer);
			}
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private void simplify(int maxEdgesToKeep) {
		int countWM = 0, countVW = 0;
		int segments = mapWM.getSegments();
		double threshold = 0.001;
		while (segments > maxEdgesToKeep && threshold < maxThreshold) {
			WM.simplify(mapWM, threshold);
			countWM += segments - mapWM.getSegments();
			segments = mapWM.getSegments();
			VW.removeAllSmall(mapWM, vwAngleThreshold);
			countVW += segments - mapWM.getSegments();
			segments = mapWM.getSegments();
			threshold = Math.min(threshold * 2, maxThreshold);
		}

		double vwAtEnd = 0;
		while (segments > maxEdgesToKeep) {
			VW.Next(mapWM, false);
			segments--;
			if (segments != mapWM.getSegments()) {
				System.out.println("Unable to continue removing lines");
				break;
			}
			vwAtEnd++;
		}
		System.out.println(countWM + " " + countVW + " " + vwAtEnd);
		System.out.println("Map is correct: " + mapWM.isTopoCorrect());

		segments = mapVW.getSegments();
		while (segments > maxEdgesToKeep) {
			VW.Next(mapVW, false);
			segments--;
			if (segments != mapVW.getSegments()) {
				// System.out.println("Unable to continue removing lines");
				break;
			}
		}
	}

	// einlesen
	public void readData(String lineFile, String pointsFile) {
		mapWM = new MapData();
		mapVW = new MapData();
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
			mapWM.lines.add(currentLine);
			mapVW.lines.add(currentLine.copy());
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
				mapWM.pois.add(new Point(new Double(string_point_split[0])
						.doubleValue(), new Double(string_point_split[1])
						.doubleValue()));
			}
		}
		mapWM.setFinal();
		mapVW.pois.addAll(mapWM.pois);
		mapVW.setFinal();
	}

	private List<GMLObject> updateBends() {
		List<GMLObject> gml = new ArrayList<GMLObject>();
		for (int i = 0; i < mapWM.lines.size(); i++) {
			gml.addAll(mapWM.lines.get(i).findBends());
		}
		gml.addAll(mapWM.pois);
		return gml;
	}

	private void display() {
		panelWM = new GMLPanel();
		panelWM.map = mapWM;
		panelWM.setGMLObjects(updateBends());
		// JFrame frame = GMLPanel.showPanelInWindow(panelWM);
		// frame.addKeyListener(this);
		System.out.println("MapWM is correct: " + mapWM.isTopoCorrect());

		panelVW = new GMLPanel();
		panelVW.map = mapVW;
		List<GMLObject> gml = new ArrayList<GMLObject>();
		gml.addAll(mapVW.lines);
		gml.addAll(mapVW.pois);
		panelVW.setGMLObjects(gml);
		frame = GMLPanel.showPanelInWindow(panelWM, panelVW);
		frame.addKeyListener(this);
		System.out.println("MapWM is correct: " + mapWM.isTopoCorrect());
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == e.VK_DELETE) {
			panelWM.eliminateSelectedBend();
			panelWM.setGMLObjects(updateBends());
			panelWM.repaint();
			panelVW.eliminateSelectedBend();
			List<GMLObject> gml = new ArrayList<GMLObject>();
			gml.addAll(mapVW.lines);
			gml.addAll(mapVW.pois);
			panelVW.setGMLObjects(gml);
			panelVW.repaint();
		} else if (e.getKeyCode() == e.VK_E) {
			panelWM.exaggerateSelectedBend();
			panelWM.setGMLObjects(updateBends());
			panelWM.repaint();
			panelVW.exaggerateSelectedBend();
			List<GMLObject> gml = new ArrayList<GMLObject>();
			gml.addAll(mapVW.lines);
			gml.addAll(mapVW.pois);
			panelVW.setGMLObjects(gml);
			panelVW.repaint();
		} else if (e.getKeyCode() == e.VK_C) {
			System.out.println("Combine....");
			panelWM.combineSelectedBend();
			panelWM.setGMLObjects(updateBends());
			panelWM.repaint();
			System.out.println("Endcombine....");
			System.out.println("Combine....");
			panelVW.combineSelectedBend();
			List<GMLObject> gml = new ArrayList<GMLObject>();
			gml.addAll(mapVW.lines);
			gml.addAll(mapVW.pois);
			panelVW.setGMLObjects(gml);
			panelVW.repaint();
			System.out.println("Endcombine....");
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	private static double threshold = 50000;// .001;
	// Massachusetts shouldn't exaggerate bottom left bend
	private static double maxThreshold = 1.024E8 * 4;
	private static double edges = -1;

	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getKeyChar() == ' ') {
			// for (int i = 0; i < 20; ++i)
			// VW.Next(map);
			WM.simplify(mapWM, threshold);
			threshold = Math.min(threshold * 2, maxThreshold);
			panelWM.setGMLObjects(updateBends());
			panelWM.repaint();
		}
		if (e.getKeyChar() == 'v' || e.getKeyChar() == 'V') {
			for (int i = 0; i < 20; ++i)
				VW.Next(mapVW, false);
			System.out.println("There are now " + mapVW.getSegments()
					+ " segments left");
			System.out.println();
			// WM.simplify(map, threshold);
			// threshold = Math.min(threshold * 2, maxThreshold);
			List<GMLObject> gml = new ArrayList<GMLObject>();
			gml.addAll(mapVW.lines);
			gml.addAll(mapVW.pois);
			panelVW.setGMLObjects(gml);
			panelVW.repaint();
		}
		if (e.getKeyChar() == 's' || e.getKeyChar() == 'S') {
			if (edges < 0) {
				edges = .75 * mapWM.getSegments();
			}
			simplify((int) edges);
			edges *= .75;

			System.out.println("There are now " + mapWM.getSegments()
					+ " segments left in WM and \n" + mapVW.getSegments()
					+ " segments left in VW");
			System.out.println();
			panelWM.setGMLObjects(updateBends());
//			List<GMLObject> gml = new ArrayList<GMLObject>();
////			for (Line l: mapVW.lines){
////				gml.add(l.copy());
////			}
//			gml.addAll(mapVW.lines);
//			gml.addAll(mapVW.pois);
//			System.out.println("VW Lines: "+mapVW.lines.size());
//			System.out.println("VW POIs: "+mapVW.pois.size());
//			System.out.println("GML: "+gml.size()+" ("+(mapVW.lines.size()+mapVW.pois.size())+")");
//			panelVW.setGMLObjects(gml);
			panelWM.repaint();
			panelVW.repaint();
		}
		System.out.println("Map is correct: " + mapWM.isTopoCorrect());
		System.out.println("Map is correct: " + mapVW.isTopoCorrect());
	}

}

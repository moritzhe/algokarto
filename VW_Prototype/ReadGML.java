import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ReadGML {

	public static void main(String[] args) {
		Scanner scan;

		List<GMLObject> gml = new ArrayList<GMLObject>();
		int ind = 2;// choose test file
		File file = new File("lines_out" + ind + ".txt");
		try {
			scan = new Scanner(file);
			readGML(scan, gml);
			scan.close();
			file = new File("points_out" + ind + ".txt");
			scan = new Scanner(file);
			readGML(scan, gml);
			scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		GMLPanel panel = new GMLPanel();
		panel.list = gml;
		panel.calculateGMLBounds();
		GMLPanel.showPanelInWindow(panel);
	}

	public static void readGML(Scanner scan, List<GMLObject> gml) {
		// use <, >, \r, and \n as delimiters
		scan.useDelimiter("[<>]|\\r|\\n");

		String str;
		while (scan.hasNext()) {
			try {
				// read id
				do {
					str = scan.next().trim();
				} while (str.length() == 0 && scan.hasNext());
				// for some reason, we tend to finish a file here
				if (!scan.hasNext()) {
					break;
				}
				int id = Integer.parseInt(str.substring(0, str.length() - 1));

				// read point(s)
				List<Point2D.Double> list = new ArrayList<Point2D.Double>();
				// count nest-level of gml tags
				int nesting = 0;
				do {
					str = scan.next();
					// Assumption: Token is either (/)gml:blahblah or
					// empty or "#,# #,# ... #,#"
					if (str.startsWith("gml")) {
						nesting++;
					} else if (str.startsWith("/gml")) {
						nesting--;
					} else if (str.length() != 0) {
						StringTokenizer tk = new StringTokenizer(str, ", ");
						while (tk.hasMoreElements()) {
							list.add(new Point2D.Double(Double.parseDouble(tk
									.nextToken()), Double.parseDouble(tk
									.nextToken())));
						}
					}
				} while (nesting != 0);// use GML structure to find out when
										// object is over
				// add to list
				gml.add(new GMLObject(id, list, new Color(
						(int) (Math.random() * 200),
						(int) (Math.random() * 200),
						(int) (Math.random() * 200))));
			} catch (NumberFormatException e) {
				System.out.println("Number format error");
				e.printStackTrace();
				break;
			}
		}
		scan.reset();
	}

}
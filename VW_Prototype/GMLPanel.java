import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Draw GML Objects
 */
public class GMLPanel extends JPanel implements MouseListener {
	/** GML Objects to draw. */
	private List<GMLObject> list;
	/** Transformation to keep them in-window. */
	AffineTransform at = new AffineTransform();
	/** Bounds of GML Objects */
	double xMin, xMax, yMin, yMax;
	/** Most recent dimension of this JPanel */
	Dimension d = new Dimension(0, 0);
	/** Buffer for mouse Selection */
	BufferedImage selectionBuffer;
	/** selected Object */
	int currentlySelectedObject;

	public MapData map;

	public void setGMLObjects(List<GMLObject> gml) {
		list = gml;
		calculateGMLBounds();
		System.out.println("Changed GML: size: " + gml.size());
	}

	/**
	 * Bounds of the GML Objects (call once at beginning)
	 */
	private void calculateGMLBounds() {
		xMin = Double.POSITIVE_INFINITY;
		xMax = Double.NEGATIVE_INFINITY;
		yMin = Double.POSITIVE_INFINITY;
		yMax = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < list.size(); i++) {
			Rectangle r = list.get(i).getBounds();
			xMin = Math.min(xMin, r.x);
			xMax = Math.max(xMax, r.x + r.width);
			yMin = Math.min(yMin, r.y);
			yMax = Math.max(yMax, r.y + r.height);
		}
	}

	/**
	 * How to scale/translate graphical representation of GML to fit in-window.
	 * Change if JPanel's size has changed since last check.
	 * 
	 * @return
	 */
	protected AffineTransform calculateAffine() {
		// check if need to update
		Dimension d2 = this.getSize();
		if (d2.equals(d)) {
			return at;
		}

		// update
		d = d2;
		at.setToIdentity();
		// move away from top left corner
		at.translate(d.getWidth() * .05, d.getHeight() * .05);

		// amount to scale by
		double scale = Math.min(d.getWidth() * .9 / (xMax - xMin),
				d.getHeight() * .9 / (yMax - yMin));

		// y flips because computer's positive y is down
		at.scale(scale, -scale);

		// Move (xMin,yMax) to origin (yMax because computer's positive y is
		// down)
		at.translate(-xMin, -yMax);
		return at;
	}

	/** Draw GML Objects */
	public void paintObjects(Graphics g, boolean drawHash) {
		System.out.println(this.hashCode() + " " + list.size());
		Graphics2D gr = (Graphics2D) g;

		// save G2D affine
		AffineTransform save = gr.getTransform();
		// change G2D affine to ours
		gr.transform(calculateAffine());
		//
		// System.out.println("AT: "+at.getScaleX()+","+at.getScaleY());
		Stroke defaultStroke = new BasicStroke((float) (2.0 / Math.abs(at
				.getScaleX())) /*(float) .01*/);// gr.getStroke();
		gr.setStroke(defaultStroke);
		Stroke selectedStroke = new BasicStroke((float) (4.0 / Math.abs(at
				.getScaleX())));// 2000
		// Draw objects with this new transform
		for (int i = 0; i < list.size(); i++) {
			if ((list.get(i).hashCode() & 0xFFFFFF) == currentlySelectedObject) {
				gr.setStroke(selectedStroke);
			}

			if (drawHash) {
				// TODO: Draw thicker so that it's easier to select
				list.get(i).drawWithColor(gr,
						new Color(list.get(i).hashCode() & 0xFFFFFF));
			} else {
				list.get(i).draw(gr);
			}
			gr.setStroke(defaultStroke);
		}
		// reset to old transform just in case
		gr.setTransform(save);
	}

	/** Draw the GML Objects */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintObjects(g, false);
		selectionBuffer = new BufferedImage(getWidth(), getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics selectionGraphics = selectionBuffer.createGraphics();
		paintObjects(selectionGraphics, true);
		// Debug Code: Display selection Buffer
		// g.drawImage(selectionBuffer, 0, 0, null);
	}

	public static JFrame showPanelInWindow(GMLPanel panel) {
		panel.setPreferredSize(new Dimension(600, 600));
		JFrame window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.add(panel);
		window.pack();
		window.setVisible(true);
		panel.addMouseListener(panel);
		return window;
	}

	public static JFrame showPanelInWindow(GMLPanel panel, GMLPanel panel2) {
		panel.setPreferredSize(new Dimension(600, 600));
		panel2.setPreferredSize(new Dimension(600, 600));
		JFrame window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.add(panel, BorderLayout.WEST);
		window.add(panel2, BorderLayout.EAST);
		window.pack();
		window.setVisible(true);
		panel.addMouseListener(panel);
		panel2.addMouseListener(panel2);
		return window;
	}

	double mousex, mousey;
	double time;

	@Override
	public void mouseClicked(MouseEvent e) {
		// Fuer dopple-click, raus zoomen.
		double time2 = System.nanoTime();
		if (time2 - time < 200000000L) {
			d = null;
			calculateAffine();
			repaint();
		}
		System.out.println(time2 + " " + (time2 - time));
		time = time2;

		// Selection
		int selection = selectionBuffer.getRGB(e.getX(), e.getY()) & 0xFFFFFF;
		System.out.println(selection);
		if (selection != currentlySelectedObject) {
			currentlySelectedObject = selection;
			repaint();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mousex = e.getX();
		mousey = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		double x2 = e.getX(), y2 = e.getY();

		// click or invalid window
		if (mousex == x2 || mousey == y2) {
			return;
		}

		double xMin = Math.min(mousex, x2);
		double xMax = Math.max(mousex, x2);
		double yMin = Math.min(mousey, y2);
		double yMax = Math.max(mousey, y2);

		// zooming in
		AffineTransform at = new AffineTransform();
		// following operations occur in backwards order:

		// origin to middle
		at.translate(d.getWidth() * .5, d.getHeight() * .5);

		// amount to scale by
		double scale = Math.min(d.getWidth() * .9 / (xMax - xMin),
				d.getHeight() * .9 / (yMax - yMin));

		// System.out.println(xMin + " " + xMax + " " + yMin + " " + yMax);

		// y flips because computer's positive y is down
		at.scale(scale, scale);

		// Move middle to origin
		at.translate(-(xMin + xMax) / 2.0, -(yMin + yMax) / 2.0);

		at.concatenate(this.at);
		this.at = at;// at.concatenate(this.at);
		// this.at.concatenate(at);

		repaint();
	}

	public Bend getSelectedBend() {
		Bend bend = null;
		for (GMLObject obj : list) {
			if ((obj.hashCode() & 0xFFFFFF) == currentlySelectedObject
					&& obj instanceof Bend) {
				bend = (Bend) obj;
			}
		}
		if (bend == null) {
			return null;
		}
		return bend;
	}

	public void eliminateSelectedBend() {
		Bend bend = getSelectedBend();
		if (bend != null) {
			bend.eliminate(map);
		} else {
			System.out.println("Keine Bend ausgewaehlt!");
		}
	}

	public void exaggerateSelectedBend() {
		Bend bend = getSelectedBend();
		if (bend != null) {
			bend.exaggerate();
		} else {
			System.out.println("Keine Bend ausgewaehlt!");
		}
	}

	public Bend nextBend(Bend bend) {
		for (GMLObject obj : list) {
			if (obj instanceof Bend) {
				Bend bendToTest = (Bend) obj;
				if (bendToTest.points.get(0) == bend.points.get(bend.points
						.size() - 2)) {
					if (bendToTest.points.get(1) == bend.points.get(bend.points
							.size() - 1)) {
						return bendToTest;
					}
				}
			}
		}
		return null;
	}

	public Bend prevBend(Bend bend) {
		for (GMLObject obj : list) {
			if (obj instanceof Bend) {
				Bend bendToTest = (Bend) obj;
				if (bend.points.get(0) == bendToTest.points
						.get(bendToTest.points.size() - 2)) {
					if (bend.points.get(1) == bendToTest.points
							.get(bendToTest.points.size() - 1)) {
						return bendToTest;
					}
				}
			}
		}
		return null;
	}

	public void combineSelectedBend() {
		Bend bend = getSelectedBend();
		if (bend != null) {
			Bend bendB = nextBend(bend);
			if (bendB != null) {
				Bend bendC = prevBend(bend);
				if (bendC != null) {
					Bend.ignoreChecks = false;
					bendC.combine(bend, bendB);
					Bend.ignoreChecks = false;
				} else {
					System.out.println("Kein vorheriger Nachbar");
				}
			} else {
				System.out.println("Kein erster Nachbar");
			}
		} else {
			System.out.println("Keine Bend ausgewaehlt!");
		}
	}

}
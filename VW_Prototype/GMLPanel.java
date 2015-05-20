import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Draw GML Objects
 */
public class GMLPanel extends JPanel implements MouseListener {
	/** GML Objects to draw. */
	List<GMLObject> list;
	/** Transformation to keep them in-window. */
	AffineTransform at = new AffineTransform();
	/** Bounds of GML Objects */
	double xMin, xMax, yMin, yMax;
	/** Most recent dimension of this JPanel */
	Dimension d = new Dimension(0, 0);

	/**
	 * Bounds of the GML Objects (call once at beginning)
	 */
	protected void calculateGMLBounds() {
		xMin = list.get(0).x;
		xMax = xMin;
		yMin = list.get(0).y;
		yMax = yMin;
		for (int i = 0; i < list.size(); i++) {
			Rectangle r = list.get(i).path.getBounds();
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

	/** Draw the GML Objects */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D gr = (Graphics2D) g;
		// save G2D affine
		AffineTransform save = gr.getTransform();
		// change G2D affine to ours
		gr.transform(calculateAffine());
		// Draw objects with this new transform
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).isPoint) {
				gr.setColor(Color.RED);
				drawPoint(gr, new Point2D.Double(list.get(i).x, list.get(i).y));
			} else {
				gr.setColor(list.get(i).color);
				// gr.setColor(new Color((int)(Math.random()*256),
				// (int)(Math.random()*256), (int)(Math.random()*256)));
				gr.draw(list.get(i).path);
			}
		}
		// reset to old transform just in case
		gr.setTransform(save);
	}

	/**
	 * Draw Points as crosses so you can see them
	 * 
	 * @param g
	 * @param p
	 *            Point to draw
	 */
	protected void drawPoint(Graphics2D g, Point2D.Double p) {
		// plus form
		double crossWidth = 3;

		// change so affine transform doesn't ruin
		double xDiff = crossWidth / at.getScaleX(), yDiff = crossWidth
				/ at.getScaleY();

		// draw
		Line2D.Double l = new Line2D.Double(p.x - xDiff, p.y, p.x + xDiff, p.y);
		g.draw(l);
		l = new Line2D.Double(p.x, p.y - yDiff, p.x, p.y + yDiff);
		g.draw(l);
	}

	public static JFrame showPanelInWindow(GMLPanel panel) {
		panel.setPreferredSize(new Dimension(600, 600));
		JFrame window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.add(panel);
		window.pack();
		window.setVisible(true);
		window.addMouseListener(panel);
		return window;
	}

	double mousex, mousey;

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		mousex = e.getX();
		mousey = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		double x2 = e.getX(), y2 = e.getY();

		double xMin = Math.min(mousex, x2);
		double xMax = Math.max(mousex, x2);
		double yMin = Math.min(mousey, y2);
		double yMax = Math.max(mousey, y2);

		Dimension d2 = this.getSize();
		// update
		d = d2;
		at.setToIdentity();

		//
		//
		//
		//

		// update
		d = d2;
		at = calculateAffine();
		// move away from top left corner
		at.translate(d.getWidth() * .05, d.getHeight() * .05);

		// amount to scale by
		double scale = Math.min(d.getWidth() * .9 / (xMax - xMin),
				d.getHeight() * .9 / (yMax - yMin));

		System.out.println(xMin + " " + xMax + " " + yMin + " " + yMax);

		// y flips because computer's positive y is down
		at.scale(scale, scale);

		// Move (xMin,yMin) to origin
		at.translate(-xMin, -yMin);

		// move away from top left corner
		at.translate(d.getWidth() * .05, d.getHeight() * .05);

		// amount to scale by
		scale = Math.min(d.getWidth() * .9 / (this.xMax - this.xMin),
				d.getHeight() * .9 / (this.yMax - this.yMin));

		// y flips because computer's positive y is down
		at.scale(scale, -scale);

		// Move (xMin,yMax) to origin (yMax because computer's positive y is
		// down)
		at.translate(-this.xMin, -this.yMax);

		repaint();
	}

}
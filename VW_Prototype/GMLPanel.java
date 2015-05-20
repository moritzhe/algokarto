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
		xMin = Double.POSITIVE_INFINITY;
		xMax = Double.NEGATIVE_INFINITY;
		yMin = Double.POSITIVE_INFINITY;
		yMax = Double.NEGATIVE_INFINITY;
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
				drawPoint(gr, (Point2D.Double)list.get(i).path.getCurrentPoint());
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
	 * @param point
	 *            Point to draw
	 */
	protected void drawPoint(Graphics2D g, Point2D.Double point) {
		// plus form
		double crossWidth = 3;

		// change so affine transform doesn't ruin
		double xDiff = crossWidth / at.getScaleX(), yDiff = crossWidth
				/ at.getScaleY();

		// draw
		Line2D.Double l = new Line2D.Double(point.x - xDiff, point.y, point.x + xDiff, point.y);
		g.draw(l);
		l = new Line2D.Double(point.x, point.y - yDiff, point.x, point.y + yDiff);
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
	double time;

	@Override
	public void mouseClicked(MouseEvent e) {
		//Fuer dopple-click, raus zoomen.
		double time2 = System.nanoTime();
		if (time2-time<1500000){
			d = null;
			calculateAffine();
			repaint();
		}
		System.out.println(time2+" "+(time2-time));
		time = time2;
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
		
		//click or invalid window
		if (mousex == x2 || mousey == y2){
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

		System.out.println(xMin + " " + xMax + " " + yMin + " " + yMax);

		// y flips because computer's positive y is down
		at.scale(scale, scale);

		// Move middle to origin
		at.translate(-(xMin+xMax)/2.0, -(yMin+yMax)/2.0);

		at.concatenate(this.at);
		this.at = at;//at.concatenate(this.at);
		//this.at.concatenate(at);

		repaint();
	}

}
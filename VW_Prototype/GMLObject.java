import java.awt.Graphics2D;
import java.awt.Rectangle;

public interface GMLObject {
	public Rectangle getBounds();

	public void draw(Graphics2D g);
}

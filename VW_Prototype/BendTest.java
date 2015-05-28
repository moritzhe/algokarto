import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class BendTest {
	private static MapData map;
	private static List<Bend> bends;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		WM wm = new WM();

		// *--*.*--*
		// ...|.|
		// *--*.*--*
		// |.......|
		// *-------*
		Line line = new Line();
		line.add(new Point(0, 0));
		line.add(new Point(5, 0));
		line.add(new Point(5, 5));
		line.add(new Point(0, 5));
		line.add(new Point(0, 10));
		line.add(new Point(11, 10));
		line.add(new Point(11, 5));
		line.add(new Point(6, 5));
		line.add(new Point(6, 0));
		line.add(new Point(11, 0));

		bends = new ArrayList<Bend>();
		bends.addAll(wm.findBends(line));
	}

	@Test
	public void testUpdate() {
		fail("Not yet implemented");
	}

	@Test
	public void testBendConstructors() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsPositive() throws InterruptedException {

		// Alternating positivity
		assertEquals(bends.get(0).isPositive(), bends.get(2).isPositive());
		assertNotEquals(bends.get(0).isPositive(), bends.get(1).isPositive());

		// List<Bend> b = new ArrayList<Bend>();
		List<GMLObject> g = new ArrayList<GMLObject>();
		// for (int i = 0; i < bends.size(); i++) {
		// if (bends.get(i).size() > 5) {
		// b.add(bends.get(i));
		// System.out.println(bends.get(i));
		// g.add(bends.get(i));
		// }
		// }
		g.addAll(bends);

		GMLPanel panel = new GMLPanel();
		panel.setGMLObjects(g);
		GMLPanel.showPanelInWindow(panel);
		// Thread.sleep(10000);
		// fail("Not yet implemented");
	}

	@Test
	public void testArea() {
		int[] area = {25,55,25};
		for (int i = 0; i < bends.size(); i++) {
			assertEquals(area[i], bends.get(i).area(), 0.00001);
		}
	}

	@Test
	public void testGetCircumference() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCompactness() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAdjustedSize() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAvgCurve() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testToString(){
		String[] points = {
				"[Point [x=0.0, y=0.0], Point [x=5.0, y=0.0], Point [x=5.0, y=5.0], Point [x=0.0, y=5.0]]",
				"[Point [x=5.0, y=5.0], Point [x=0.0, y=5.0], Point [x=0.0, y=10.0], Point [x=11.0, y=10.0], Point [x=11.0, y=5.0], Point [x=6.0, y=5.0]]",
				"[Point [x=11.0, y=5.0], Point [x=6.0, y=5.0], Point [x=6.0, y=0.0], Point [x=11.0, y=0.0]]" };
		for (int i = 0; i < bends.size(); i++) {
			assertEquals(points[i], bends.get(i).toString());
		}
	}

	@Test
	public void testSimilarityTo() {
		fail("Not yet implemented");
	}

}

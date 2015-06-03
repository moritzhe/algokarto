import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class WMTest {
	private static MapData map;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		KartoTest karto = new KartoTest();
		String data = "";// "testdaten2/";
		map = karto.readData(data + "lines_out2.txt", data + "points_out2.txt");
	}

	@Test
	public void testFindBends() {
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

		List<Bend> bends = new ArrayList<Bend>();
		bends.addAll(line.findBends());

		// Found all the bends
		assertEquals(3, bends.size());

		// Alternating positivity
		assertEquals(bends.get(0).isPositive(), bends.get(2).isPositive());
		assertNotEquals(bends.get(0).isPositive(), bends.get(1).isPositive());

		String[] points = {
				"[Point [x=0.0, y=0.0], Point [x=5.0, y=0.0], Point [x=5.0, y=5.0], Point [x=0.0, y=5.0]]",
				"[Point [x=5.0, y=5.0], Point [x=0.0, y=5.0], Point [x=0.0, y=10.0], Point [x=11.0, y=10.0], Point [x=11.0, y=5.0], Point [x=6.0, y=5.0]]",
				"[Point [x=11.0, y=5.0], Point [x=6.0, y=5.0], Point [x=6.0, y=0.0], Point [x=11.0, y=0.0]]" };
		for (int i = 0; i < bends.size(); i++) {
			assertEquals(points[i], bends.get(i).toString());
		}
	}

}

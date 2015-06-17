import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.BeforeClass;
import org.junit.Test;

public class BendTest {
	private static MapData map;
	private static List<Bend> bends;
	private static Line line;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// *--*.*--*
		// ...|.|
		// *--*.*--*
		// |.......|
		// *-------*
		line = new Line();
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
		bends.addAll(line.findBends());
	}

	@Test
	public void testFindBends() {
		Line line;
		List<Bend> bends;

		// *--*.*--*
		// ...|.|
		// *--*.*--*
		// |.......|
		// *-------*
		line = new Line();
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
		bends.addAll(line.findBends());

		// Found all the bends
		assertEquals(3, bends.size());

		// Alternating positivity
		assertEquals(bends.get(0).isPositive(), bends.get(2).isPositive());
		assertNotEquals(bends.get(0).isPositive(), bends.get(1).isPositive());

		String[] points = { "[0.0,0.0, 5.0,0.0, 5.0,5.0, 0.0,5.0]",
				"[5.0,5.0, 0.0,5.0, 0.0,10.0, 11.0,10.0, 11.0,5.0, 6.0,5.0]",
				"[11.0,5.0, 6.0,5.0, 6.0,0.0, 11.0,0.0]" };
		for (int i = 0; i < bends.size(); i++) {
			assertEquals(points[i], bends.get(i).toString());
		}
	}

	@Test
	public void testSelfIntersection() {
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

		assertFalse(line.lineSelfIntersects());
		for (int i = 0; i < bends.size(); i++) {
			bends.get(i).recordEndPoints();
			assertFalse(bends.get(i).lineSelfIntersects());
			if (i < bends.size() - 1)
				assertTrue(bends.get(i).lineIntersects(bends.get(i + 1)));
		}
		// System.out.println(
		// bends.get(0).size()+" "+bends.get(bends.size()-1).size());
		assertFalse(bends.get(0).lineIntersects(bends.get(bends.size() - 1)));

		// intersect at point
		line.get(1).x = 6;
		assertTrue(line.lineSelfIntersects());
		assertTrue(bends.get(0).lineIntersects(bends.get(bends.size() - 1)));
		// intersect on segment
		line.get(1).y = 1;
		assertTrue(line.lineSelfIntersects());
		assertTrue(bends.get(0).lineIntersects(bends.get(bends.size() - 1)));
		// intersect across segment
		line.get(1).y = 2;
		assertTrue(line.lineSelfIntersects());
		assertTrue(bends.get(0).lineIntersects(bends.get(bends.size() - 1)));
		// intersect with neighbor
		line.get(1).x = 2;
		line.get(1).y = 5;
		assertTrue(line.lineSelfIntersects());
		// intersect with neighbor
		line.get(1).x = -1;
		assertTrue(line.lineSelfIntersects());
		// weird don't intersect with neighbor
		line.get(1).x = 5.5;
		assertFalse(line.lineSelfIntersects());

		// reset 1
		line.get(1).x = 5;
		line.get(1).y = 0;
		// divide by zero intersect with neighbor
		line.get(2).x = 0;
		line.get(2).y = 6;
		assertTrue(line.lineSelfIntersects());
		// divide by zero non-intersect with neighbor
		line.get(2).y = 4;
		assertFalse(line.lineSelfIntersects());
	}

	@Test
	public void testInitLineIntersects2() {
		Line l1 = initLineIntersectTest("-8016058.200610452,5311347.903380732 -8019151.323981643,5310538.289127092 -8017661.646555842,5307130.838740712 -8019303.386406063,5302365.652452341");
		Line l2 = initLineIntersectTest("-8019303.386406063,5302365.652452341 -8018884.602481692,5302354.101632512 -8019744.100270113,5295296.213758642 -8021524.210247393,5285677.481547092 -8021614.267715442,5283586.0880270805 -8021874.198726443,5277652.756805111 -8021999.878431553,5274447.117107532 -8015732.034502433,5274305.441073522 -8015438.151046742,5272366.439750611 -8015171.540866292,5270610.356014232 -8013987.546762212,5262404.209090772 -8013249.498538252,5257654.433460792 -8012203.206644283,5250921.165587872 -8008911.266662553,5250800.705827912 -8007132.047241203,5239267.532868552");

		Line l3 = initLineIntersectTest("-8018884.602481692,5302354.101632512 -8019744.100270113,5295296.213758642 -8021524.210247393,5285677.481547092 -8021614.267715442,5283586.0880270805 -8021874.198726443,5277652.756805111 -8021999.878431553,5274447.117107532 -8015732.034502433,5274305.441073522 -8015438.151046742,5272366.439750611 -8015171.540866292,5270610.356014232 -8013987.546762212,5262404.209090772 -8013249.498538252,5257654.433460792 -8012203.206644283,5250921.165587872 -8008911.266662553,5250800.705827912 -8007132.047241203,5239267.532868552");
		
		for (int i = 0; i < l1.size(); i++) {
			if (l2.points.contains(l1.get(i)))
				System.out.println(l1.get(i));
		}

		assertTrue(l2.get(0).equals(l1.get(l1.size() - 1)));
		assertFalse(l1.lineIntersects(l3));

		System.out.println(l1.lineIntersects(l2));
		System.out.println(l2.lineIntersects(l1));
	}

	public Line initLineIntersectTest(String l1) {
		StringTokenizer tk = new StringTokenizer(l1, " ,");
		Line line1 = new Line();
		while (tk.hasMoreElements()) {
			line1.add(new Point(Double.parseDouble(tk.nextToken()), Double
					.parseDouble(tk.nextToken())));
		}
		line1.recordEndPoints();
		return line1;
	}

	@Test
	public void testInitLineIntersects() {
		// Line line1 = new Line();
		// line1.add(new Point(-7929990.868386692, 5326092.063381992));
		// line1.add(new Point(-7921564.205572623, 5336939.267557371));
		// line1.add(new Point(-7925134.666920332, 5360880.566086502));
		// line1.add(new Point(-7921952.265317532, 5364977.4508753205));
		// line1.recordEndPoints();
		// assertEquals(
		// "-7929990.868386692,5326092.063381992 -7921564.205572623,5336939.267557371 -7925134.666920332,5360880.566086502 -7921952.265317532,5364977.4508753205",
		// line1.output());
		// Line line2 = new Line();
		// line2.add(new Point(-7931258.129469883, 5324489.661286581));
		// line2.add(new Point(-7929990.868386692, 5326092.063381992));
		// line2.recordEndPoints();

		Line line1 = initLineIntersectTest("-7929990.868386692,5326092.063381992 -7921564.205572623,5336939.267557371 -7925134.666920332,5360880.566086502 -7921952.265317532,5364977.4508753205");
		Line line2 = initLineIntersectTest("-7931258.129469883,5324489.661286581 -7929990.868386692,5326092.063381992");

		assertEquals(
				"-7931258.129469883,5324489.661286581 -7929990.868386692,5326092.063381992",
				line2.output());

		// start testing:
		assertEquals(line1.get(0), line2.get(1));

		assertFalse(line2.lineIntersects(line1));
		assertFalse(line1.lineIntersects(line2));
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
		int[] area = { 25, 55, 25 };
		for (int i = 0; i < bends.size(); i++) {
			assertEquals(area[i], bends.get(i).area(), 0.00001);
		}
	}

	@Test
	public void testGetTheta() {
		// *--*.*--*
		// ...|.|
		// *--*.*--*
		// |.......|
		// *-------*
		assertEquals(Math.PI * 3 / 4.0,
				Bend.getTheta(line.get(0), line.get(1), line.get(3)), 0.00001);
	}

	@Test
	public void testGetCircumference() {
		int[] circ = { 20, 32, 20 };
		for (int i = 0; i < bends.size(); i++) {
			assertEquals(circ[i], bends.get(i).getCircumference(), 0.00001);
		}
	}

	@Test
	public void testGetCompactness() {
		double[] cmp = new double[3];
		for (int i = 0; i < cmp.length; i++) {
			cmp[i] = bends.get(i).area() / //
					(Math.PI
							* (bends.get(i).getCircumference() / (2 * Math.PI)) * (bends
							.get(i).getCircumference() / (2 * Math.PI)));
		}
		for (int i = 0; i < bends.size(); i++) {
			assertEquals(cmp[i], bends.get(i).getCompactness(), 0.00001);
		}
	}

	@Test
	public void testGetAdjustedSize() {
		// area * .75 / cmp
		// this test assumes that area and cmp work
		double[] adj = new double[3];
		for (int i = 0; i < adj.length; i++) {
			adj[i] = bends.get(i).area() * .75 / //
					bends.get(i).getCompactness();
		}
		for (int i = 0; i < bends.size(); i++) {
			assertEquals(adj[i], bends.get(i).getAdjustedSize(), 0.00001);
		}
	}

	@Test
	public void testGetAvgCurve() {
		double[] values = { Math.PI / 15, 2 * Math.PI / 31, Math.PI / 15 };
		for (int i = 0; i < bends.size(); i++) {
			assertEquals(values[i], bends.get(i).getAvgCurve(), 0.00001);
		}
	}

	@Test
	public void testToString() {
		String[] points = { "[0.0,0.0, 5.0,0.0, 5.0,5.0, 0.0,5.0]",
				"[5.0,5.0, 0.0,5.0, 0.0,10.0, 11.0,10.0, 11.0,5.0, 6.0,5.0]",
				"[11.0,5.0, 6.0,5.0, 6.0,0.0, 11.0,0.0]" };
		for (int i = 0; i < bends.size(); i++) {
			assertEquals(points[i], bends.get(i).toString());
		}
	}

	@Test
	public void testSimilarityTo() {
		assertEquals(0, bends.get(0).similarityTo(bends.get(2)), 0.00001);
		assertNotEquals(0, bends.get(0).similarityTo(bends.get(1)), 0.00001);
	}

}

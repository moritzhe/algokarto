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

		Line line2 = new Line();
		for (int i = 0; i < 4; i++) {
			line2.add(line.get(i));
		}
		line2.add(line.get(0));
		line2.recordEndPoints();
		assertFalse(line2.lineSelfIntersects());

		List<Bend> bends = new ArrayList<Bend>();
		bends.addAll(line.findBends());

		line.recordEndPoints();
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
	public void testCombine() throws InterruptedException {
		Line l = buildLine("-7904854.148129132,5479760.574212922 -7913125.512000472,5639318.712064935 -7914671.586948496,5638713.406730251 -7918320.646461458,5629990.2047726605 -7918677.511660429,5629617.074550603 -7920184.957445677,5628968.975492208 -7929928.125875301,5631425.562728266 -7933018.840511505,5634919.880464475 -7933230.154538637,5637450.517671044 -7935446.8592694765,5638785.05185969 -7937255.800994843,5637941.973634643 -7942208.071181912,5634114.978447226 -7946006.181481572,5629131.269252394 -7960405.245702753,5592436.189595492 -7963222.373018992,5585989.173801249 -7964627.511612123,5580291.629678585 -7965915.248670715,5652062.116228914 -7967536.817560943,5558743.731241781 -7971786.661760952,5554638.171240042 -7973330.663098252,5551207.841425101 -7973291.701276482,5548816.432500831 -7970116.980718543,5541076.836191372 -7969164.865113793,5538419.8399568815 -7968011.595189172,5536749.791630021 -7967106.233770553,5536657.626729332 -7965602.975366882,5532505.572895382 -7965393.138126732,5529848.149037962 -7964525.180057012,5527842.704570712 -7963431.243420993,5526945.377570062 -7964820.733305072,5524364.815156432 -7966162.578447093,5523151.888856331 -7967140.965451682,5522987.925104262 -7969436.707310303,5523410.842444532 -7970098.279044092,5522655.802557592 -7970172.195185982,5521491.835494742 -7967604.7224503225,5519266.144345381 -7967684.538525223,5518415.427537882 -7968580.549106612,5516975.1819736315 -7967975.0823961925,5513728.277354931 -7969042.636312903,5512981.2037922405 -7970374.351381262,5511188.977616661 -7973774.048630082,5509194.215578902 -7974193.611790882,5509283.145598852 -7974366.824918563,5509827.6278993515 -7973312.851979732,5510402.464020572 -7974075.501811152,5510722.783532331 -7975636.535030553,5509312.219064961 -7977296.976555222,5504114.2236520415 -7978053.726453633,5503363.906986782 -7979321.655453773,5503118.424511772 -7980084.305285193,5501272.392874062 -7981968.9442643225,5499771.052233032 -7987903.831596472,5498631.778177782 -7988866.967830823,5498667.339491862");
		Line l2 = buildLine("-7904854.148129132,5479760.574212922 -7913125.512000472,5639318.712064935 -7914671.586948496,5638713.406730251 -7918320.646461458,5629990.2047726605 -7918677.511660429,5629617.074550603 -7920184.957445677,5628968.975492208 -7929928.125875301,5631425.562728266 -7933018.840511505,5634919.880464475 -7933230.154538637,5637450.517671044 -7935446.8592694765,5638785.05185969 -7937255.800994843,5637941.973634643 -7942208.071181912,5634114.978447226 -7946006.181481572,5629131.269252394 -7960405.245702753,5592436.189595492 -7962431.550450839,5590315.949821274 -7963045.866475817,5588945.181718634 -7962620.498766142,5583777.919789523 -7959427.637934612,5577452.463883242 -7959516.804846732,5576306.897645752 -7963181.553803143,5570914.071153882 -7964505.587826633,5570309.833627591 -7964909.9002172025,5569755.098096111 -7967536.817560943,5558743.731241781 -7971786.661760952,5554638.171240042 -7973330.663098252,5551207.841425101 -7973291.701276482,5548816.432500831 -7970116.980718543,5541076.836191372 -7969164.865113793,5538419.8399568815 -7968011.595189172,5536749.791630021 -7967106.233770553,5536657.626729332 -7965602.975366882,5532505.572895382 -7965393.138126732,5529848.149037962 -7964525.180057012,5527842.704570712 -7963431.243420993,5526945.377570062 -7964820.733305072,5524364.815156432 -7966162.578447093,5523151.888856331 -7967140.965451682,5522987.925104262 -7969436.707310303,5523410.842444532 -7970098.279044092,5522655.802557592 -7970172.195185982,5521491.835494742 -7967604.7224503225,5519266.144345381 -7967684.538525223,5518415.427537882 -7968580.549106612,5516975.1819736315 -7967975.0823961925,5513728.277354931 -7969042.636312903,5512981.2037922405 -7970374.351381262,5511188.977616661 -7973774.048630082,5509194.215578902 -7974193.611790882,5509283.145598852 -7974366.824918563,5509827.6278993515 -7973312.851979732,5510402.464020572 -7974075.501811152,5510722.783532331 -7975636.535030553,5509312.219064961 -7977296.976555222,5504114.2236520415 -7978053.726453633,5503363.906986782 -7979321.655453773,5503118.424511772 -7980084.305285193,5501272.392874062 -7981968.9442643225,5499771.052233032 -7987903.831596472,5498631.778177782 -7988866.967830823,5498667.339491862");
		List<Line> list = new ArrayList<Line>();
		List<GMLObject> g = new ArrayList<GMLObject>();
		list.add(l2);
		list.add(l);

		MapData map = new MapData(list, new ArrayList<Point>());
		map.setFinal();
		WM.simplify(map, 6.8719476736E7);

		GMLPanel panel = new GMLPanel();
		panel.setGMLObjects(g);
		// GMLPanel.showPanelInWindow(panel);
		// Thread.sleep(10000);
		// fail("Not yet implemented");
	}

	@Test
	public void testInitLineIntersects2() {
		Line l1 = buildLine("-8016058.200610452,5311347.903380732 -8019151.323981643,5310538.289127092 -8017661.646555842,5307130.838740712 -8019303.386406063,5302365.652452341");
		Line l2 = buildLine("-8019303.386406063,5302365.652452341 -8018884.602481692,5302354.101632512 -8019744.100270113,5295296.213758642 -8021524.210247393,5285677.481547092 -8021614.267715442,5283586.0880270805 -8021874.198726443,5277652.756805111 -8021999.878431553,5274447.117107532 -8015732.034502433,5274305.441073522 -8015438.151046742,5272366.439750611 -8015171.540866292,5270610.356014232 -8013987.546762212,5262404.209090772 -8013249.498538252,5257654.433460792 -8012203.206644283,5250921.165587872 -8008911.266662553,5250800.705827912 -8007132.047241203,5239267.532868552");

		Line l3 = buildLine("-8018884.602481692,5302354.101632512 -8019744.100270113,5295296.213758642 -8021524.210247393,5285677.481547092 -8021614.267715442,5283586.0880270805 -8021874.198726443,5277652.756805111 -8021999.878431553,5274447.117107532 -8015732.034502433,5274305.441073522 -8015438.151046742,5272366.439750611 -8015171.540866292,5270610.356014232 -8013987.546762212,5262404.209090772 -8013249.498538252,5257654.433460792 -8012203.206644283,5250921.165587872 -8008911.266662553,5250800.705827912 -8007132.047241203,5239267.532868552");

		assertTrue(l2.get(0).equals(l1.get(l1.size() - 1)));
		assertFalse(l1.lineIntersects(l3));

		assertFalse(l1.lineIntersects(l2));
		assertFalse(l2.lineIntersects(l1));
	}

	@Test
	public void testInitLineSelfIntersectsMA() throws InterruptedException {
		Line l1 = buildLine("-7922073.714881982,5177799.145523941 -7923666.140197782,5179389.033379811 -7921216.777441852,5181759.293449931");

		// System.out.println("Should be false:");
		assertFalse(l1.lineSelfIntersects());
		assertFalse(l1.lineIntersects(l1));

		// List<GMLObject> g = new ArrayList<GMLObject>();
		// g.add(l1);
		// GMLPanel panel = new GMLPanel();
		// panel.setGMLObjects(g);
		// GMLPanel.showPanelInWindow(panel);
		// Thread.sleep(10000);
	}

	public Line buildLine(String l1) {
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

		Line line1 = buildLine("-7929990.868386692,5326092.063381992 -7921564.205572623,5336939.267557371 -7925134.666920332,5360880.566086502 -7921952.265317532,5364977.4508753205");
		Line line2 = buildLine("-7931258.129469883,5324489.661286581 -7929990.868386692,5326092.063381992");

		assertEquals(
				"-7931258.129469883,5324489.661286581 -7929990.868386692,5326092.063381992",
				line2.output());

		// start testing:
		assertEquals(line1.get(0), line2.get(1));

		// System.out.println("Following should be false: ");
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
		// GMLPanel.showPanelInWindow(panel);
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

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;


public class SimplifyMapTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testWrite() {

		int id = 1;
		int maxEdges = 500;
		// new String[] { "20000", "algokarto/lines_out" + id + ".txt",
		// "algokarto/points_out" + id + ".txt", "results.txt" };
		SimplifyMap.main(new String[] { "20000000",
				"algokarto/lines_out" + id + ".txt",
				"algokarto/points_out" + id + ".txt", "results.txt" });
		

		SimplifyMap karto = new SimplifyMap();
		karto.readData("algokarto/lines_out" + id + ".txt", "algokarto/points_out" + id + ".txt");
		karto.writeFile("results.txt");
		MapData m1 = karto.mapWM;
		karto.readData("results.txt", "algokarto/points_out" + id + ".txt");
		MapData m2 = karto.mapVW;
		
		assertEquals(m2,m1);
	}

}

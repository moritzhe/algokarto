public class PretendCommandLine {

	public static void main(String[] args) {

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
		
		System.out.println("Equal: " +m1.equals(m2));
		
	}
}

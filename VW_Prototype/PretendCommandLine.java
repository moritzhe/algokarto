public class PretendCommandLine {

	public static void main(String[] args) {

		int id = 1;
		int maxEdges = 500;
		// new String[] { "20000", "algokarto/lines_out" + id + ".txt",
		// "algokarto/points_out" + id + ".txt", "results.txt" };
		SimplifyMap.main(new String[] { "300",
				"algokarto/lines_out" + id + ".txt",
				"algokarto/points_out" + id + ".txt", "results.txt" });
	}
}

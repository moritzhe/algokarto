import java.util.ArrayList;
import java.util.List;

public class WM {

	// private MapData mapWithData, testMap;

	/** In radians */
	public static final double ISOLATED_THRESHOLD = Math.PI / 4;

	public static final double RELATIVE_ISOLATED_THRESHOLD = 1 / 5.0;
	public static final double SIMILAR_THRESHOLD = 1;

	public static void simplify(MapData map, double userTolerance) {
		boolean changeHappened;
		int[] countChanges = new int[3];
		do {
			changeHappened = false;
			// detect bends
			List<List<Bend>> list = new ArrayList<List<Bend>>();
			for (Line line : map.lines) {
				list.add(line.findBends());
			}

			// loop over lines
			for (List<Bend> bends : list) {
				// loop over bends in line
				for (int i = 0; i < bends.size(); i++) {

					Bend bend = bends.get(i);
					// Wenn gross genug, ignoriere bend einfach
					if (bend.getAdjustedSize() < userTolerance) {

						// EXAGGERATE ISOLATED BEND
						// nicht erst, nicht letzt, und Nachbaren nur leicht
						// gebeugt
						if (i > 0
								&& i < bends.size() - 1
								&& bends.get(i - 1).getAvgCurve() < Math.min(
										ISOLATED_THRESHOLD,
										RELATIVE_ISOLATED_THRESHOLD
												* bend.getAvgCurve())
								&& bends.get(i + 1).getAvgCurve() < Math.min(
										ISOLATED_THRESHOLD,
										RELATIVE_ISOLATED_THRESHOLD
												* bend.getAvgCurve())) {
							changeHappened = bend.exaggerate();

							if (changeHappened) {
								countChanges[0]++;
								break;
							}
						}

						// COMBINE SIMILAR BENDS
						// nicht letzt oder zweit letzt
						// similar zu bend+2
						// groesser als bend+1
						if (i < bends.size() - 2
								&& bend.similarityTo(bends.get(i + 2)) < SIMILAR_THRESHOLD
								&& bend.getAdjustedSize() > bends.get(i + 1)
										.getAdjustedSize()) {
							// String str = bends.get(i) + "\n" + bends.get(i +
							// 2)
							// + "\n";
							changeHappened = bend.combine(bends.get(i + 1),
									bends.get(i + 2));

							if (changeHappened) {
								// System.out.println(str);
//								System.out.println("User tolerance: "
//										+ userTolerance);
								countChanges[1]++;
								break;
							}
						}

						// ELIMINATE LOCAL MINIMAL BEND
						// nicht erst, nicht letzt, und kleiner als Nachbaren
						if (i > 0
								&& i < bends.size() - 1
								&& bend.getAdjustedSize() < bends.get(i - 1)
										.getAdjustedSize()
								&& bend.getAdjustedSize() < bends.get(i + 1)
										.getAdjustedSize()) {
							changeHappened = bend.eliminate(map);

							if (changeHappened) {
								countChanges[2]++;
								break;
							}
						}
					}
				}
			}
		} while (changeHappened);
		if (SimplifyMap.VERBOSE && countChanges[0] + countChanges[1] + countChanges[2] > 0) {
			System.out.println("Made " + countChanges[0]
					+ " exaggerations,\n\t" + countChanges[1]
					+ " combinations, and\n\t" + countChanges[2]
					+ " eliminations");
			System.out.println("There are now " + map.getSegments()
					+ " segments left");
			System.out.println("User tolerance: " + userTolerance);
			System.out.println();
		}
	}
}

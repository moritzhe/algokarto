import java.util.ArrayList;
import java.util.List;

public class WM {

	private MapData mapWithData, testMap;

	protected List<Bend> findBends(Line line) {
		List<Bend> bends = new ArrayList<Bend>();
		if (line.size() < 3) {
			bends.add(new Bend(line));
			return bends;
		}
		Bend bend = new Bend(line.get(0), line.get(1), isPositive(
				line.get(0), line.get(1), line.get(2)));
		for (int i = 2; i < line.size() - 1; i++) {
			boolean pos = isPositive(line.get(i - 1), line.get(i),
					line.get(i + 1));
			bend.add(line.get(i));
			if ((pos && !bend.isPositive()) || (!pos && bend.isPositive())) {
				bends.add(bend);
				bend = new Bend(line.get(i - 1), line.get(i), pos);
			}
		}
		// Der letzte Punkt is immer auf den gleichen Bend wie der vorletzte
		bend.add(line.get(line.size() - 1));
		bends.add(bend);
		return bends;
	}

	/**
	 * 
	 * @param p1
	 *            anfangspunkt
	 * @param p3
	 *            endpunkt
	 * @return
	 */
	protected boolean isPositive(Point p1, Point p2, Point p3) {
		return (p2.x - p1.x) * (p3.y - p2.y) - (p2.y - p1.y) * (p3.x - p2.x) > 0;
	}

	
}

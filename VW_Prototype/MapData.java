import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class MapData {
	final List<Line> lines;
	final List<Point> pois;

	public MapData() {
		this(new ArrayList<Line>(), new ArrayList<Point>());
	}

	public MapData(ArrayList<Line> lines, ArrayList<Point> pois) {
		this.lines = lines;
		this.pois = pois;
	}

	public boolean checkTopology(Line line) {

		return false;
	}

	public void setFinal() {
		List<Line> lines2 = new ArrayList<Line>();
		for (Line line : lines) {
			line.recordEndPoints();

			// points and ancestors that we've seen
			Map<Point, Line> prevL = new HashMap<Point, Line>();
			Map<Point, Line> prevR = new HashMap<Point, Line>();
			Queue<Point> ptsL = new ArrayDeque<Point>();
			Queue<Point> ptsR = new ArrayDeque<Point>();

			// start searching from end of starting line
			ptsL.add(line.get(0));
			ptsR.add(line.get(line.size() - 1));

			// so we'll know when we're done
			prevL.put(line.get(0), line);
			prevR.put(line.get(line.size() - 1), line);

			while (!ptsL.isEmpty() && !ptsR.isEmpty()) {
				Point prevLEnd = ptsL.poll();
				Point prevREnd = ptsR.poll();

				for (int i = 0; i < lines2.size(); i++) {
					// current line to check against
					Line check = lines2.get(i);
					// if line adj, add OTHER endpoint to ptsL/R
					Point endL = otherEnd(check, prevLEnd), endR = otherEnd(
							check, prevREnd);

					// if endLR != null, check for cycle
					if (findCycle(check, endL, prevLEnd, prevL, prevR, ptsL)) {
						setPath(endL, prevL, prevR, line);
					} else if (findCycle(check, endR, prevREnd, prevR, prevL,
							ptsR)) {
						setPath(endL, prevL, prevR, line);
					}
				}
			}
		}
	}

	private void setPath(Point middle, Map<Point, Line> prevL,
			Map<Point, Line> prevR, Line line) {
		List<Line> path = new ArrayList<Line>();

		Point cur = middle;
		while (prevL.get(cur) != line) {

		}

	}

	private boolean findCycle(Line check, Point end, Point prevEnd,
			Map<Point, Line> thisPrev, Map<Point, Line> otherPrev,
			Queue<Point> pts) {
		// special case (line wasn't actually adjacent)
		if (end == null)
			return false;
		pts.add(end);
		// invalid cycle
		if (!thisPrev.containsKey(end))
			thisPrev.put(end, check);
		// valid cycle
		if (otherPrev.containsKey(end))
			return true;
		return false;
	}

	private Point otherEnd(Line line, Point pt) {
		if (line.get(0).equals(pt)) {
			return line.get(line.size() - 1);
		}
		if (line.get(line.size() - 1).equals(pt)) {
			return line.get(0);
		}
		return null;
	}

	public int getSegments() {
		int segments = 0;
		for (Line line : lines) {
			segments += line.size() - 1;
		}
		return segments;
	}
}

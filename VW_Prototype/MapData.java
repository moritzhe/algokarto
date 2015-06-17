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

	public MapData(List<Line> lines, List<Point> pois) {
		this.lines = lines;
		this.pois = pois;
	}

	public boolean isTopoCorrect() {
		int count = 0;
		for (Line l : lines) {
			if (!l.stillTopologicallyCorrect())
				count++;
		}
		System.out.println("Map size: "+lines.size()+ " \nBad lines: "+count);
		if (count>0)
			return false;
		return true;
	}

	public boolean checkTopology(Line line) {
		return line.stillTopologicallyCorrect();
	}

	public void setFinal() {
		for (Line line : lines) {
			line.setMapData(this);
		}
		for (Line line : lines) {
			// so that we can later check they haven't moved
			line.recordEndPoints();

			// a loop that should stay topologically correct
			// and form a cycle with this line
			setPath(line);

			line.setInitContains(pois);
		}
	}

	private void setPath(Line line) {
		// points and ancestors that we've seen
		Map<Point, Line> prevL = new HashMap<Point, Line>();
		Map<Point, Line> prevR = new HashMap<Point, Line>();
		Queue<Point> ptsL = new ArrayDeque<Point>();
		Queue<Point> ptsR = new ArrayDeque<Point>();

		Point start = line.get(0), end = line.get(line.size() - 1);

		// special case:
		if (start.equals(end)) {
			line.setComplement(new Line());
			return;
		}

		// start searching from end of starting line
		ptsL.add(end);
		ptsR.add(start);

		// so we'll know when we're done
		prevL.put(end, line);
		prevR.put(start, line);

		while (true) {
			Point prevLEnd = ptsL.poll();
			Point prevREnd = ptsR.poll();

			for (Line check : lines) {
				// if line to check is adj, find OTHER endpoint
				Point endL = otherEnd(check, prevLEnd), endR = otherEnd(check,
						prevREnd);

				// if endLR != null, check for cycle
				if (findCycle(check, endL, prevL, prevR, ptsL)) {
					setPath(endL, prevL, prevR, line);
					return;
				} else if (findCycle(check, endR, prevR, prevL, ptsR)) {
					setPath(endR, prevL, prevR, line);
					return;
				}
			}
		}
	}

	private void setPath(Point middle, Map<Point, Line> prevL,
			Map<Point, Line> prevR, Line line) {
		List<Line> path1 = new ArrayList<Line>();

		Point cur = middle;
		while (prevL.get(cur) != line) {
			path1.add(prevL.get(cur));
			Point cur2 = otherEnd(prevL.get(cur), cur);
			if (cur2 == null) {
				System.out.println(cur + " " + prevL.get(cur));
			}
			cur = cur2;
		}

		List<Line> path = new ArrayList<Line>();
		for (int i = path1.size() - 1; i >= 0; i--) {
			path.add(path1.get(i));
		}

		cur = middle;
		while (prevR.get(cur) != line) {
			path.add(prevR.get(cur));
			cur = otherEnd(prevR.get(cur), cur);
		}

		Point prevEnd = line.get(line.size() - 1);
		List<Point> complement = new ArrayList<Point>();
		complement.add(prevEnd);
		for (Line l : path) {
			if (prevEnd.equals(l.get(0))) {
				complement.remove(complement.size() - 1);
				complement.addAll(l.points);
				prevEnd = l.get(l.size() - 1);
			} else {
				for (int i = l.size() - 2; i >= 0; i--) {
					complement.add(l.get(i));
				}
				prevEnd = l.get(0);
			}
		}
		complement.remove(complement.size() - 1);

		Line comp = new Line(complement);
		line.setComplement(comp);
	}

	private boolean findCycle(Line check, Point end, Map<Point, Line> thisPrev,
			Map<Point, Line> otherPrev, Queue<Point> pts) {
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
		try {
			if (line.get(0).equals(pt)) {
				return line.get(line.size() - 1);
			}
			if (line.get(line.size() - 1).equals(pt)) {
				return line.get(0);
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			System.out.println(pt + " pt");
			System.out.println(line + " line");
			System.out.println(line.get(0) + " first");

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

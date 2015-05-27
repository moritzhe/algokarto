public class VW {
	// Entfernt einen Punkt
	public static void Next(MapData map) {
		double leastEffectiveArea = Double.POSITIVE_INFINITY;
		int leastEffLineIdx = -1;
		int leastEffPointIdx = -1;
		for (int k = 0; k < map.lines.size(); ++k) {
			pointLoop: for (int i = 1; i < map.lines.get(k).size() - 1; ++i) {
				double area = effectiveArea(map.lines.get(k).get(i - 1),
						map.lines.get(k).get(i), map.lines.get(k).get(i + 1));
				if (area < leastEffectiveArea) {
					//Suche nach Staedten / POIs, die Reduzierung verhindern
					for (Point poi : map.pois) {
						if (inTrig(map.lines.get(k).get(i - 1), map.lines
								.get(k).get(i), map.lines.get(k).get(i + 1),
								poi)) {
							continue pointLoop;
						}
					}
					//Suche nach Linienschnitten, die Reduzierung verhindern
					for (Line line : map.lines){
						for (Point point : line.points) {
							Point p1 = map.lines.get(k).get(i - 1);
							Point p2 = map.lines.get(k).get(i);
							Point p3 = map.lines.get(k).get(i + 1);
							if (inTrig(p1, p2, p3, point) && p1 != point
									&& p2 != point && p3!= point) {
								continue pointLoop;
							}
						}
					}

					leastEffectiveArea = area;
					leastEffLineIdx = k;
					leastEffPointIdx = i;

				}
			}

		}
		if (leastEffPointIdx != -1)
			map.lines.get(leastEffLineIdx).remove(leastEffPointIdx);

	}

	private static double effectiveArea(Point a, Point b, Point c) {
		return Math.abs(signedEffectiveArea(a, b, c));
	}

	private static double signedEffectiveArea(Point a, Point b, Point c) {
		double effArea = ((a.getX() - c.getX()) * (b.getY() - a.getY()) - (a
				.getX() - b.getX()) * (c.getY() - a.getY())) / 2.0;
		return effArea;
	}

	private static boolean inTrig(Point p1, Point p2, Point p3, Point p) {
		double alpha = signedEffectiveArea(p, p2, p3)
				/ signedEffectiveArea(p1, p2, p3);
		double beta = signedEffectiveArea(p, p3, p1)
				/ signedEffectiveArea(p1, p2, p3);
		double gamma = 1.0 - alpha - beta;
		return (alpha >= 0 && beta >= 0 && gamma > 0);
	}

}

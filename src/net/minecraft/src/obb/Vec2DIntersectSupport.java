package net.minecraft.src.obb;

public class Vec2DIntersectSupport {

	public static Point getIntercectPoint(Vec2D v1, Vec2D v2) {
		Point viFrom = v1.getFrom();
		Point v1To = v1.getTo();
		Point v2From = v2.getFrom();
		Point v2To = v2.getTo();

		return getIntersectPoint(viFrom, v1To, v2From, v2To);
	}

	private static Point getIntersectPoint(Point viFrom, Point v1To, Point v2From, Point v2To) {
		double aFromX = viFrom.x;
		double aFromY = viFrom.y;
		double aToX = v1To.x;
		double aToY = v1To.y;

		double bFromX = v2From.x;
		double bFromY = v2From.y;
		double bToX = v2To.x;
		double bToY = v2To.y;

		Double dR = getIntersectValue(aFromX, aFromY, aToX, aToY, bFromX, bFromY, bToX, bToY);
		if (dR == null) {
			return null;
		}

		return viFrom.addedPoint(new Point(viFrom, v1To).multiplyPoint(dR));
	}

	private static Double getIntersectValue(double aFromX, double aFromY, double aToX, double aToY, double bFromX, double bFromY, double bToX, double bToY) {
		double denomin = (aToX - aFromX) * (bToY - bFromY) - (aToY - aFromY) * (bToX - bFromX);
		if (denomin == 0) { // 平行
			return null;
		}

		Point tempPointBA = new Point(aFromY - aFromX, bFromY - aFromY);

		double dR = ((bToY - bFromY) * tempPointBA.x - (bToX - bFromX) * tempPointBA.y) / denomin;
		double dS = ((aToY - aFromY) * tempPointBA.x - (aToX - aFromX) * tempPointBA.y) / denomin;

		if (dR < 0 || 1 < dR || dS < 0 || 1 < dS) {
			// 交点なし
			return null;
		}

		return dR;
	}
}

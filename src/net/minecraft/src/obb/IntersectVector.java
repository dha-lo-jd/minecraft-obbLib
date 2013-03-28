package net.minecraft.src.obb;

import net.minecraft.src.MathHelper;

public class IntersectVector {

	public enum PointType {
		FROM, TO, ;
	}

	public final Point from;

	public final Point to;

	public IntersectVector(double fX, double fY, double tX, double tY) {
		this(new Point(fX, fY), new Point(tX, tY));
	}

	public IntersectVector(Point from, Point to) {
		this.from = from;
		this.to = to;
	}

	public Point get(PointType key) {
		switch (key) {
		case FROM:
			return from;
		case TO:
			return to;
		}
		throw new IllegalArgumentException(key.toString());
	}

	public Point getIntercectPoint(IntersectVector v) {

		double aFromX = from.x;
		double aFromY = from.y;
		double aToX = to.x;
		double aToY = to.y;

		double bFromX = v.from.x;
		double bFromY = v.from.y;
		double bToX = v.to.x;
		double bToY = v.to.y;

		double denomin = (aToX - aFromX) * (bToY - bFromY) - (aToY - aFromY) * (bToX - bFromX);
		if (denomin == 0) { // 平行
			return null;
		}

		Point tempPointBA = new Point(v.from.x - from.x, v.from.y - from.y);

		double dR = ((bToY - bFromY) * tempPointBA.x - (bToX - bFromX) * tempPointBA.y) / denomin;
		double dS = ((aToY - aFromY) * tempPointBA.x - (aToX - aFromX) * tempPointBA.y) / denomin;

		if (dR < 0 || 1 < dR || dS < 0 || 1 < dS) {
			// 交点なし
			return null;
		}

		return from.addedPoint(new Point(from, to).multiplyPoint(dR));
	}

	public double getLength() {
		return MathHelper.sqrt_double(getLengthSquare());
	}

	public double getLengthSquare() {
		Point p = new Point(from, to);
		return p.x * p.x + p.y * p.y;
	}

	public Point getUnitVectorPoint() {
		double length = getLength();
		Point p = new Point(from, to);

		return p.multiplyPoint(1.0D / length);
	}

	/**
	 * より小さいxy値のfrom、より大きいxy値のtoをもつ新しいVectorを返す
	 * 
	 * @param vector
	 * @return
	 */
	public IntersectVector marge(IntersectVector vector) {
		Point newFrom = null;
		Point newTo = null;
		for (PointType pointType : PointType.values()) {
			Point aPoint = get(pointType);
			Point bPoint = vector.get(pointType);
			switch (pointType) {
			case FROM:
				newFrom = Point.minPoint(aPoint, bPoint);
				break;
			case TO:
				newTo = Point.maxPoint(aPoint, bPoint);
				break;
			}
		}
		IntersectVector result = new IntersectVector(newFrom, newTo);
		return result;
	}

	@Override
	public String toString() {
		return "V[from" + from + ", to" + to + "]";
	}
}
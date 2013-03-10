package net.minecraft.src.obb;

import net.minecraft.src.MathHelper;

public class IntersectVector {

	@Override
	public String toString() {
		return "V[from" + from + ", to" + to + "]";
	}

	public IntersectVector(double fX, double fY, double tX, double tY) {
		this(new Point(fX, fY), new Point(tX, tY));
	}

	public IntersectVector(Point from, Point to) {
		this.from = from;
		this.to = to;
	}

	public final Point from;
	public final Point to;

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

	public Point getIntercectPoint(IntersectVector v) {

		double aFromX = from.x;
		double aFromY = from.y;
		double aToX = to.x;
		double aToY = to.y;

		double bFromX = v.from.x;
		double bFromY = v.from.y;
		double bToX = v.to.x;
		double bToY = v.to.y;

		double denomin = (aToX - aFromX) * (bToY - bFromY) - (aToY - aFromY)
				* (bToX - bFromX);
		if (denomin == 0) { // 平行
			return null;
		}

		Point tempPointBA = new Point(v.from.x - from.x, v.from.y - from.y);

		double dR = ((bToY - bFromY) * tempPointBA.x - (bToX - bFromX)
				* tempPointBA.y)
				/ denomin;
		double dS = ((aToY - aFromY) * tempPointBA.x - (aToX - aFromX)
				* tempPointBA.y)
				/ denomin;

		if (dR < 0 || 1 < dR || dS < 0 || 1 < dS) {
			// 交点なし
			return null;
		}

		return from.addedPoint(new Point(from, to).multiplyPoint(dR));
	}
}
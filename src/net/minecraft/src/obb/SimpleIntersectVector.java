package net.minecraft.src.obb;

public class SimpleIntersectVector extends Vec2DImpl implements IntersectVector {
	public SimpleIntersectVector(double fX, double fY, double tX, double tY) {
		super(fX, fY, tX, tY);
	}

	public SimpleIntersectVector(Point from, Point to) {
		super(from, to);
	}

	@Override
	public Point getIntercectPoint(IntersectVector v) {
		return Vec2DIntersectSupport.getIntercectPoint(this, v);
	}

	@Override
	public Vec2D getVector() {
		return this;
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
			switch (pointType) {
			case FROM: {
				Point bPoint = vector.getVector().getFrom();
				newFrom = Point.minPoint(aPoint, bPoint);
				break;
			}
			case TO: {
				Point bPoint = vector.getVector().getTo();
				newTo = Point.maxPoint(aPoint, bPoint);
				break;
			}
			}
		}
		IntersectVector result = new SimpleIntersectVector(newFrom, newTo);
		return result;
	}
}
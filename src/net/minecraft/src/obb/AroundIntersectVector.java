package net.minecraft.src.obb;

import net.minecraft.src.obb.AroundWorker.Around.AxisType;
import net.minecraft.src.obb.AroundWorker.Around.OffsetType;

public abstract class AroundIntersectVector implements IntersectVector {

	final AxisType axisType;
	final OffsetType offsetType;

	final double axis;

	final double from;
	final double to;

	final Point fPoint;

	final Point tPoint;
	final Vec2D vec;

	final Point uPoint;

	public AroundIntersectVector(AxisType axisType, OffsetType offsetType, double axis, double from, double to) {
		this.axisType = axisType;
		this.offsetType = offsetType;
		this.axis = axis;
		this.from = from;
		this.to = to;

		switch (axisType) {
		case X:
			fPoint = new Point(axis, from);
			tPoint = new Point(axis, to);
			uPoint = new Point(0, 1);
			break;
		case Y:
			fPoint = new Point(from, axis);
			tPoint = new Point(to, axis);
			uPoint = new Point(1, 0);
			break;
		default:
			throw new InternalError();
		}

		vec = new Vec2DImpl(fPoint, tPoint);
	}

	@Override
	public Point getFrom() {
		return fPoint;
	}

	@Override
	public Point getIntercectPoint(IntersectVector v) {
		return Vec2DIntersectSupport.getIntercectPoint(this, v);
	}

	@Override
	public double getLength() {
		return to - from;
	}

	@Override
	public double getLengthSquare() {
		return getLength() * getLength();
	}

	@Override
	public Point getTo() {
		return tPoint;
	}

	@Override
	public Point getUnitVectorPoint() {
		return uPoint;
	}

	@Override
	public Vec2D getVector() {
		return vec;
	}

}

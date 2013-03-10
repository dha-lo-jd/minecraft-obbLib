package net.minecraft.src.obb;

public class Point {
	@Override
	public String toString() {
		return "P[" + x + "," + y + "](" + super.toString() + ")";
	}

	public final double x;
	public final double y;

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Point(Point from, Point to) {
		this.x = to.x - from.x;
		this.y = to.y - from.y;
	}

	public Point addedPoint(Point point) {
		return new Point(x + point.x, y + point.y);
	}

	Point multiplyPoint(double factor) {
		return new Point(x * factor, y * factor);
	}

}
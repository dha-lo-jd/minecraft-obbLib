package net.minecraft.src.obb;

public interface IntersectVector extends Vec2D {

	public abstract Point getIntercectPoint(IntersectVector v);

	Vec2D getVector();

}
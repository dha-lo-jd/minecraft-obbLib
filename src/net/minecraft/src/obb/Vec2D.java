package net.minecraft.src.obb;


public interface Vec2D {

	public abstract Point getFrom();

	public abstract double getLength();

	public abstract double getLengthSquare();

	public abstract Point getTo();

	public abstract Point getUnitVectorPoint();

}
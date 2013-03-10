package net.minecraft.src.obb;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Entity;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Vec3D;

public class IntersectRect {

	Point from;
	Point to;

	public IntersectVector topVec;
	public IntersectVector bottomVec;
	public IntersectVector leftVec;
	public IntersectVector rightVec;

	private class IntersectResult {
		private IntersectVector collideVec;
		private Point collideP;

		private IntersectResult(IntersectVector collideVec, Point collideP) {
			this.collideVec = collideVec;
			this.collideP = collideP;
		}
	}

	public Point pos;

	private void initVector() {
		topVec = new IntersectVector(from.x, from.y, to.x, from.y);
		rightVec = new IntersectVector(to.x, from.y, to.x, to.y);
		bottomVec = new IntersectVector(to.x, to.y, from.x, to.y);
		leftVec = new IntersectVector(from.x, to.y, from.x, from.y);
	}

	public IntersectRect(AxisAlignedBB box, double offsetX, double offsetZ) {
		double minX = box.minX;
		double maxX = box.maxX;
		double minY = box.minZ;
		double maxY = box.maxZ;

		if (maxX < minX) {
			double m = minX;
			minX = maxX;
			maxX = m;
		}
		if (maxY < minY) {
			double m = minY;
			minY = maxY;
			maxY = m;
		}

		this.pos = new Point(offsetX, offsetZ);
		this.from = new Point(minX, minY);
		this.to = new Point(maxX, maxY);
		initVector();
	}

	public IntersectRect(AxisAlignedBB box) {
		double minX = box.minX;
		double maxX = box.maxX;
		double minY = box.minZ;
		double maxY = box.maxZ;

		if (maxX < minX) {
			double m = minX;
			minX = maxX;
			maxX = m;
		}
		if (maxY < minY) {
			double m = minY;
			minY = maxY;
			maxY = m;
		}

		this.pos = getCenter(minX, maxX, minY, maxY);
		this.from = new Point(minX - pos.x, minY - pos.y);
		this.to = new Point(maxX - pos.x, maxY - pos.y);
		initVector();
	}

	private Point getCenter(double minX, double maxX, double minY, double maxY) {
		double x = (maxX - minX) / 2;
		double y = (maxY - minY) / 2;
		return new Point(minX + x, minY + y);
	}

	public static Point getZeroAxisPoint(Point point, float rad) {
		float f = MathHelper.cos(-rad);
		float f1 = MathHelper.sin(-rad);
		double d = point.x * (double) f + point.y * (double) f1;
		double d2 = point.y * (double) f - point.x * (double) f1;
		return new Point(d, d2);
	}

	public Point getCollisionPoint(IntersectVector vector,
			IntersectVector collidedVector) {
		Point p = vector.getIntercectPoint(collidedVector);
		return p;
	}

	public Vec3D getCollisionPoint(Entity entity, float yaw) {
		Point prevPosP = new Point(entity.prevPosX - pos.x, entity.prevPosZ
				- pos.y);
		prevPosP = getZeroAxisPoint(prevPosP, yaw);

		Point posP = new Point(entity.posX - pos.x, entity.posZ - pos.y);
		posP = getZeroAxisPoint(posP, yaw);

		IntersectVector vector = new IntersectVector(prevPosP, posP);

		Vec3D v3dSlide = getCollisionPoint(vector);
		if (v3dSlide == null) {
			return null;
		}

		v3dSlide.rotateAroundY(yaw);

		return Vec3D
				.createVectorHelper(v3dSlide.xCoord + pos.x - entity.prevPosX,
						0, v3dSlide.zCoord + pos.y - entity.prevPosZ);
	}

	public Vec3D getSlidePoint(Entity entity, float yaw) {
		Point prevPosP = new Point(entity.prevPosX - pos.x, entity.prevPosZ
				- pos.y);
		prevPosP = getZeroAxisPoint(prevPosP, yaw);

		Point posP = new Point(entity.posX - pos.x, entity.posZ - pos.y);
		posP = getZeroAxisPoint(posP, yaw);

		IntersectVector vector = new IntersectVector(prevPosP, posP);

		Vec3D v3dSlide = getSlidePoint(vector);
		if (v3dSlide == null) {
			return null;
		}

		v3dSlide.rotateAroundY(yaw);

		return Vec3D
				.createVectorHelper(v3dSlide.xCoord + pos.x - entity.prevPosX,
						0, v3dSlide.zCoord + pos.y - entity.prevPosZ);
	}

	public void slideToEntity(Entity entity, double radius) {
		double rX = entity.prevPosX - pos.x;
		double rY = entity.prevPosZ - pos.y;

		double rLen = MathHelper.sqrt_double(rX * rX + rY * rY);

		double radLen = radius;
		if (radLen > rLen - 0.5) {
			radLen = rLen - 0.5;
		}
		double fraction = radLen / rLen;
		fraction = fraction * 0.9;

		slidePos(rX * fraction, rY * fraction);
	}

	public void slidePos(double x, double y) {
		pos = new Point(pos.x + x, pos.y + y);
	}

	public Vec3D getCollisionPoint(IntersectVector vector) {

		IntersectResult intersectResult = getIntersectResult(vector);
		if (intersectResult == null) {
			return null;
		}
		IntersectVector collideVec = intersectResult.collideVec;
		Point collideP = intersectResult.collideP;

		Point r = vector.from.addedPoint(collideP);

		Vec3D v3dSlide = Vec3D.createVectorHelper(collideP.x, 0, collideP.y);

		return v3dSlide;
	}

	public boolean isInField(Vec3D pos, double range, float yaw) {
		return false;
	}

	public Vec3D bounceInField(Vec3D pos, double range, double out, float yaw) {
		Point p = new Point(pos.xCoord - this.pos.x, pos.zCoord - this.pos.y);
		p = getZeroAxisPoint(p, yaw);
		double bounceX = p.x;
		double bounceY = p.y;
		IntersectVector collideVec = null;
		collideVec = topVec;
		if (p.y > collideVec.from.y - range && p.y < collideVec.from.y) {
			bounceY = collideVec.from.y - out;
			System.out.println("topVec");
		}
		collideVec = bottomVec;
		if (p.y > collideVec.from.y && p.y < collideVec.from.y + range) {
			bounceY = collideVec.from.y + out;
			System.out.println("bottomVec");
		}
		collideVec = leftVec;
		if (p.x > collideVec.from.x - range && p.x < collideVec.from.x) {
			bounceX = collideVec.from.x - out;
			System.out.println("leftVec");
		}
		collideVec = rightVec;
		if (p.x > collideVec.from.x && p.x < collideVec.from.x + range) {
			bounceX = collideVec.from.x + out;
			System.out.println("rightVec");
		}

		Vec3D v3d = Vec3D.createVectorHelper(bounceX, 0, bounceY);
		v3d.rotateAroundY(yaw);
		return Vec3D.createVectorHelper(v3d.xCoord + this.pos.x - pos.xCoord,
				0, v3d.zCoord + this.pos.y - pos.zCoord);
	}

	private IntersectResult getIntersectResult(IntersectVector vector) {
		IntersectVector collideVec = null;
		Point collideP = null;
		if (collideP == null && vector.from.y <= topVec.from.y
				&& vector.to.y >= topVec.from.y) {
			collideVec = topVec;
			collideP = getCollisionPoint(vector, collideVec);
		}
		if (collideP == null && vector.from.x <= leftVec.from.x
				&& vector.to.x >= leftVec.from.x) {
			collideVec = leftVec;
			collideP = getCollisionPoint(vector, collideVec);
		}
		if (collideP == null && vector.from.y >= bottomVec.from.y
				&& vector.to.y <= bottomVec.from.y) {
			collideVec = bottomVec;
			collideP = getCollisionPoint(vector, collideVec);
		}
		if (collideP == null && vector.from.x >= rightVec.from.x
				&& vector.to.x <= rightVec.from.x) {
			collideVec = rightVec;
			collideP = getCollisionPoint(vector, collideVec);
		}
		if (collideP == null || collideVec == null) {
			return null;
		}
		return new IntersectResult(collideVec, collideP);
	}

	public Vec3D getSlidePoint(IntersectVector vector) {

		IntersectResult intersectResult = getIntersectResult(vector);
		if (intersectResult == null) {
			return null;
		}
		IntersectVector collideVec = intersectResult.collideVec;
		Point collideP = intersectResult.collideP;

		Point v = collideVec.getUnitVectorPoint();
		Point p = new Point(collideP, vector.to);
		Vec3D v3dU = Vec3D.createVectorHelper(v.x, 0, v.y);
		Vec3D v3dP = Vec3D.createVectorHelper(p.x, 0, p.y);
		double len = v3dP.dotProduct(v3dU);

		Point slideP = new Point(v3dU.xCoord * len, v3dU.zCoord * len);

		Point r = vector.from.addedPoint(slideP);
		// Point r = collideP.addedPoint(slideP);

		Vec3D v3dSlide = Vec3D.createVectorHelper(r.x, 0, r.y);

		return v3dSlide;
	}
}

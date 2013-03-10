package net.minecraft.src.obb;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Entity;
import net.minecraft.src.RotateSupport;
import net.minecraft.src.Vec3D;

public class OBBCube {

	Vec3D pos; // 中心座標
	Vec3D radius; // 半径

	Vec3D axisX;
	Vec3D axisY;
	Vec3D axisZ;

	private OBBCube(Vec3D pos, Vec3D radius, Vec3D axisX, Vec3D axisY,
			Vec3D axisZ) {
		super();
		this.pos = pos;
		this.radius = radius;
		this.axisX = axisX;
		this.axisY = axisY;
		this.axisZ = axisZ;
	}

	public static OBBCube createOBBCube(Entity entity) {
		return createOBBCube(entity.boundingBox,
				RotateSupport.toRadF(entity.rotationYaw),
				RotateSupport.toRadF(entity.rotationPitch));
	}

	public static OBBCube createOBBCube(AxisAlignedBB AABBCube, float yaw,
			float pitch) {
		Vec3D radius = getRadiusFromAABB(AABBCube);
		Vec3D pos = getPosFromAABB(AABBCube, radius);

		return createOBBCube(AABBCube, pos, yaw, pitch);
	}

	public static OBBCube createOBBCube(AxisAlignedBB AABBCube, Vec3D pos,
			float yaw, float pitch) {

		if (AABBCube == null) {
			return null;
		}

		Vec3D radius = getRadiusFromAABB(AABBCube);

		Vec3D axisX = Vec3D.createVectorHelper(1, 0, 0);
		axisX.rotateAroundY(yaw);
		axisX.rotateAroundX(pitch);

		Vec3D axisY = Vec3D.createVectorHelper(0, 1, 0);
		axisY.rotateAroundY(yaw);
		axisY.rotateAroundX(pitch);

		Vec3D axisZ = Vec3D.createVectorHelper(0, 0, 1);
		axisZ.rotateAroundY(yaw);
		axisZ.rotateAroundX(pitch);

		OBBCube cube = new OBBCube(pos, radius, axisX, axisY, axisZ);

		return cube;
	}

	public boolean isCollisionCircle(double radius, Vec3D pos) {

		Vec3D lengthVector = this.pos.subtract(pos);

		Vec3D[] axes = new Vec3D[] { axisX, axisY, axisZ, };

		for (Vec3D vec3d : axes) {
			if (compareOBBSeparateAxis(vec3d, radius, lengthVector)) {
				return false;
			}
		}

		return true;
	}

	public boolean isCollisionOBB(OBBCube cube) {

		if (cube == null) {
			return false;
		}

		Vec3D lengthVector = pos.subtract(cube.pos);

		Vec3D[] axes = new Vec3D[] { axisX, axisY, axisZ, cube.axisX,
				cube.axisY, cube.axisZ, axisX.crossProduct(cube.axisX),
				axisX.crossProduct(cube.axisY), axisX.crossProduct(cube.axisZ),
				axisY.crossProduct(cube.axisX), axisY.crossProduct(cube.axisY),
				axisY.crossProduct(cube.axisZ), axisZ.crossProduct(cube.axisX),
				axisZ.crossProduct(cube.axisY), axisZ.crossProduct(cube.axisZ), };

		for (Vec3D vec3d : axes) {
			if (compareOBBSeparateAxis(vec3d, cube, lengthVector)) {
				return false;
			}
		}

		return true;
	}

	private boolean compareOBBSeparateAxis(Vec3D axis, OBBCube cube,
			Vec3D lengthVector) {
		double lengthB = cube.lengthSegmentOnSeparateAxis(axis);
		return compareOBBSeparateAxis(axis, lengthB, lengthVector);
	}

	private boolean compareOBBSeparateAxis(Vec3D axis, double lengthB,
			Vec3D lengthVector) {

		double length = Math.abs(lengthVector.dotProduct(axis));

		double lengthA = lengthSegmentOnSeparateAxis(axis);

		return length > lengthA + lengthB;
	}

	private double lengthSegmentOnSeparateAxis(Vec3D axis) {
		double r1 = Math.abs(axis.dotProduct(axisX) * radius.xCoord);
		double r2 = Math.abs(axis.dotProduct(axisY) * radius.yCoord);
		double r3 = Math.abs(axis.dotProduct(axisZ) * radius.zCoord);
		return r1 + r2 + r3;
	}

	public static Vec3D getPosFromAABB(AxisAlignedBB AABBCube, Vec3D radius) {
		double x = (AABBCube.maxX - radius.xCoord);
		double y = (AABBCube.maxY - radius.yCoord);
		double z = (AABBCube.maxZ - radius.zCoord);

		return Vec3D.createVectorHelper(x, y, z);
	}

	public static Vec3D getRadiusFromAABB(AxisAlignedBB AABBCube) {
		double x = (AABBCube.maxX - AABBCube.minX) / 2;
		double y = (AABBCube.maxY - AABBCube.minY) / 2;
		double z = (AABBCube.maxZ - AABBCube.minZ) / 2;

		return Vec3D.createVectorHelper(x, y, z);
	}
}

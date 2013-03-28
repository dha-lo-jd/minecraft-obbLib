package net.minecraft.src.obb;

import java.util.EnumMap;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Entity;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Vec3D;
import net.minecraft.src.obb.AroundWorker.Around;
import net.minecraft.src.obb.AroundWorker.Around.AxisType;
import net.minecraft.src.obb.AroundWorker.Around.OffsetType;
import net.minecraft.src.obb.AroundWorker.Direction;
import net.minecraft.src.obb.AroundWorker.Offset;
import net.minecraft.src.obb.AroundWorker.OffsetIndex;

public class IntersectRect {

	private static class InitVectorDto {
		Double fX;
		Double fY;
		Double tX;
		Double tY;
	}

	private class IntersectResult {
		private IntersectVector collideVec;
		private Point collideP;

		private IntersectResult(IntersectVector collideVec, Point collideP) {
			this.collideVec = collideVec;
			this.collideP = collideP;
		}
	}

	public static Point getZeroAxisPoint(Point point, float rad) {
		float f = MathHelper.cos(-rad);
		float f1 = MathHelper.sin(-rad);
		double d = point.x * f + point.y * f1;
		double d2 = point.y * f - point.x * f1;
		return new Point(d, d2);
	}

	public IntersectVector topVec;

	public IntersectVector bottomVec;

	public IntersectVector leftVec;

	public IntersectVector rightVec;

	public Point pos;

	private final EnumMap<Around, IntersectVector> vectorMap = AroundWorker.mapToEnum(Around.class,
			new AroundWorker.EnumMappingWorker<Around, IntersectVector>() {
				@Override
				protected IntersectVector valueOf(Around dir) {
					switch (dir) {
					case FORWARD:
						return topVec;
					case BACK:
						return bottomVec;
					case RIGHT:
						return rightVec;
					case LEFT:
						return leftVec;
					}
					return null;
				}
			}, Around.values());

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

		pos = getCenter(minX, maxX, minY, maxY);
		Point from = new Point(minX - pos.x, minY - pos.y);
		Point to = new Point(maxX - pos.x, maxY - pos.y);
		initVector(from, to);
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

		pos = new Point(offsetX, offsetZ);
		Point from = new Point(minX, minY);
		Point to = new Point(maxX, maxY);
		initVector(from, to);
	}

	public IntersectRect(EnumMap<Around, IntersectVector> rectMap, double offsetX, double offsetZ) {
		for (Around around : Around.values()) {
			IntersectVector vector = rectMap.get(around);
			switch (around) {
			case FORWARD:
				topVec = vector;
				break;
			case BACK:
				bottomVec = vector;
				break;
			case RIGHT:
				rightVec = vector;
				break;
			case LEFT:
				leftVec = vector;
				break;
			}
		}
	}

	/**
	 * posがrectの周囲range範囲内にいた場合、rectからoutだけ離れた位置に動かす
	 * 
	 * @param pos
	 * @param range
	 * @param out
	 * @param yaw
	 * @return
	 */
	public Vec3D bounceInField(Vec3D pos, final double range, final double out, float yaw) {
		final Point rP = new Point(pos.xCoord - this.pos.x, pos.zCoord - this.pos.y);
		final Point p = getZeroAxisPoint(rP, yaw);

		Double[] dto = new Double[] {
				p.x, p.y
		};
		Around.foreach(new AroundWorker.DirectionalWorker<Around, Double[]>() {
			@Override
			public void work(Around dir, Offset offset, Double[] dto) {
				IntersectVector vector = vectorMap.get(dir);
				if (vector == null) {
					return;
				}
				double outValue;
				double min = 0;
				double max = 0;
				switch (dir.getOffsetType()) {
				case NEXT:
					max = range;
					outValue = out;
					break;
				case PREV:
					min = -range;
					outValue = -out;
					break;
				default:
					throw new InternalError();
				}

				int index;
				double value;
				double vValue;
				switch (dir.getAxisType()) {
				case X:
					value = p.x;
					vValue = vector.from.x;
					index = 0;
					break;
				case Y:
					value = p.y;
					vValue = vector.from.y;
					index = 1;
					break;
				default:
					throw new InternalError();
				}

				min = vValue + min;
				max = vValue + max;

				if (min < value && value < max) {
					dto[index] = vValue + outValue;
				}
			}
		}, dto);

		Vec3D v3d = Vec3D.createVectorHelper(dto[0], 0, dto[1]);
		v3d.rotateAroundY(yaw);
		return Vec3D.createVectorHelper(v3d.xCoord + this.pos.x - pos.xCoord, 0, v3d.zCoord + this.pos.y - pos.zCoord);
	}

	public Vec3D getCollisionPoint(Entity entity, float yaw) {
		Point prevPosP = new Point(entity.prevPosX - pos.x, entity.prevPosZ - pos.y);
		prevPosP = getZeroAxisPoint(prevPosP, yaw);

		Point posP = new Point(entity.posX - pos.x, entity.posZ - pos.y);
		posP = getZeroAxisPoint(posP, yaw);

		IntersectVector vector = new IntersectVector(prevPosP, posP);

		Vec3D v3dSlide = getCollisionPoint(vector);
		if (v3dSlide == null) {
			return null;
		}

		v3dSlide.rotateAroundY(yaw);

		return Vec3D.createVectorHelper(v3dSlide.xCoord + pos.x - entity.prevPosX, 0, v3dSlide.zCoord + pos.y - entity.prevPosZ);
	}

	public Vec3D getCollisionPoint(IntersectVector vector) {

		IntersectResult intersectResult = getIntersectResult(vector);
		if (intersectResult == null) {
			return null;
		}
		Point collideP = intersectResult.collideP;

		Vec3D v3dSlide = Vec3D.createVectorHelper(collideP.x, 0, collideP.y);

		return v3dSlide;
	}

	public Point getCollisionPoint(IntersectVector vector, IntersectVector collidedVector) {
		Point p = vector.getIntercectPoint(collidedVector);
		return p;
	}

	public Vec3D getSlidePoint(Entity entity, float yaw) {
		Point prevPosP = new Point(entity.prevPosX - pos.x, entity.prevPosZ - pos.y);
		prevPosP = getZeroAxisPoint(prevPosP, yaw);

		Point posP = new Point(entity.posX - pos.x, entity.posZ - pos.y);
		posP = getZeroAxisPoint(posP, yaw);

		IntersectVector vector = new IntersectVector(prevPosP, posP);

		Vec3D v3dSlide = getSlidePoint(vector);
		if (v3dSlide == null) {
			return null;
		}

		v3dSlide.rotateAroundY(yaw);

		return Vec3D.createVectorHelper(v3dSlide.xCoord + pos.x - entity.prevPosX, 0, v3dSlide.zCoord + pos.y - entity.prevPosZ);
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

	public boolean isInField(Vec3D pos, double range, float yaw) {
		return false;
	}

	public void slidePos(double x, double y) {
		pos = new Point(pos.x + x, pos.y + y);
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

	@Override
	public String toString() {
		return "IntersectRect [topVec=" + topVec + ", bottomVec=" + bottomVec + ", leftVec=" + leftVec + ", rightVec=" + rightVec + "]";
	}

	private Point getCenter(double minX, double maxX, double minY, double maxY) {
		double x = (maxX - minX) / 2;
		double y = (maxY - minY) / 2;
		return new Point(minX + x, minY + y);
	}

	private IntersectResult getIntersectResult(final IntersectVector vector) {

		final Around.OffsetTypeMap<Point[]> offsetTypePointsMap = new Around.OffsetTypeMap<Point[]>(new Point[] {
				vector.from, vector.to
		// 上(前)か左ならfromより大きくtoより小さい判定になるから
				}, new Point[] {
						vector.to, vector.from
				// 下(後)か右ならtoより大きくfromより小さい判定になるから
				});
		IntersectResult result = new IntersectResult(null, null);
		Around.forEachTypeWork(result, new Around.OffsetTypeWorker<Point[], Void>() {
			@Override
			public Point[] work(Around around, Void in, OffsetType offsetType) {
				return offsetTypePointsMap.get(offsetType);
			}
		}, new Around.AxisTypeWorker<Double[], Point[]>() {
			@Override
			public Double[] work(Around around, Point[] in, AxisType axisType) {
				IntersectVector v = vectorMap.get(around);
				if (v == null) {
					return null;
				}
				// ここでvの該当軸の値はfromでもtoでも同値(のはず)
				switch (axisType) {
				case Y:
					return new Double[] {
							v.from.y, in[0].y, in[1].y
					// Y軸タイプならYの値
					};
				case X:
					return new Double[] {
							v.from.x, in[0].x, in[1].x
					// X軸タイプならXの値
					};
				}
				throw new InternalError("Unknown Axis Type");
			}
		}, new Around.TypeWorker<IntersectResult, Double[]>() {

			@Override
			public void work(Around around, IntersectResult dto, Double[] in) {
				if (in == null) {
					return;
				}
				if (dto.collideP != null) {
					return;// 既に衝突判定が出た
				}
				double value = in[0];
				double min = in[1];
				double max = in[2];

				// ここで実際に衝突判定と衝突点の割り出し
				if (min <= value && value <= max) {// 自明な非衝突判定
					dto.collideVec = vectorMap.get(around);
					dto.collideP = getCollisionPoint(vector, dto.collideVec);
				}
			}
		});
		return result;
	}

	private void initVector(Point from, Point to) {
		final EnumMap<OffsetIndex, Double> offsetMapX = OffsetIndex.toMap(null, from.x, to.x);
		final EnumMap<OffsetIndex, Double> offsetMapY = OffsetIndex.toMap(null, from.y, to.y);
		AroundWorker.foreach(new AroundWorker.Each2SWorker<InitVectorDto, Around>() {
			@Override
			protected void afterWork(Around dir, Offset offset, InitVectorDto dto) {
				IntersectVector vector = new IntersectVector(dto.fX, dto.fY, dto.tX, dto.tY);
				switch (dir) {
				case FORWARD:
					topVec = vector;
					break;
				case BACK:
					bottomVec = vector;
					break;
				case RIGHT:
					rightVec = vector;
					break;
				case LEFT:
					leftVec = vector;
					break;

				default:
					break;
				}
			}

			@Override
			protected void doInternalWorkNext(Direction dir, Offset offset, InitVectorDto dto) {
				double x = offsetMapX.get(offset.xIndex);
				double y = offsetMapY.get(offset.yIndex);
				dto.tX = x;
				dto.tY = y;
			}

			@Override
			protected void doInternalWorkPrev(Direction dir, Offset offset, InitVectorDto dto) {
				double x = offsetMapX.get(offset.xIndex);
				double y = offsetMapY.get(offset.yIndex);
				dto.fX = x;
				dto.fY = y;
			}

		}, new InitVectorDto(), Around.values());
	}

}

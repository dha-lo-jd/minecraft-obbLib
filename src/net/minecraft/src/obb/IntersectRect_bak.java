package net.minecraft.src.obb;

import java.util.EnumMap;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Entity;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Vec3D;
import net.minecraft.src.obb.AroundWorker.Around;
import net.minecraft.src.obb.AroundWorker.Around.AxisMap;
import net.minecraft.src.obb.AroundWorker.Around.AxisType;
import net.minecraft.src.obb.AroundWorker.Around.OffsetType;
import net.minecraft.src.obb.AroundWorker.Direction;
import net.minecraft.src.obb.AroundWorker.Offset;
import net.minecraft.src.obb.AroundWorker.OffsetIndex;

public class IntersectRect_bak {

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

	private static class IntersectResultDto {
		Double max;
		Double min;
		Double value;

		Point from;
		Point to;
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

	private final EnumMap<Around, IntersectVector> vectorMap = EnumSupport.mapToEnum(Around.class,
			new EnumSupport.EnumMappingWorker<Around, IntersectVector>() {
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

	public IntersectRect_bak(AxisAlignedBB box) {
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

	public IntersectRect_bak(AxisAlignedBB box, double offsetX, double offsetZ) {
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

	public IntersectRect_bak(EnumMap<Around, IntersectVector> rectMap, double offsetX, double offsetZ) {
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

		AxisMap<Double> dto = new Around.AxisMap<Double>(p.x, p.y);
		Around.foreach(new AroundWorker.DirectionalWorker<Around, AxisMap<Double>>() {
			@Override
			public void work(Around dir, AxisMap<Double> dto) {
				IntersectVector vector = vectorMap.get(dir);
				if (vector == null) {
					return;
				}
				double outValue;
				double min;
				double max;
				switch (dir.getOffsetType()) {
				case POSITIVE:
					max = range;
					min = 0;
					outValue = out;
					break;
				case NEGATIVE:
					max = 0;
					min = -range;
					outValue = -out;
					break;
				default:
					throw new InternalError();
				}

				double value;
				double vValue;
				AxisType axisType = dir.getAxisType();
				switch (axisType) {
				case X:
					value = p.x;
					vValue = vector.getFrom().x;
					break;
				case Y:
					value = p.y;
					vValue = vector.getFrom().y;
					break;
				default:
					throw new InternalError();
				}

				min = vValue + min;
				max = vValue + max;

				if (min < value && value < max) {
					dto.put(axisType, vValue + outValue);
				}
			}
		}, dto);

		Vec3D v3d = Vec3D.createVectorHelper(dto.get(AxisType.X), 0, dto.get(AxisType.Y));
		v3d.rotateAroundY(yaw);
		return Vec3D.createVectorHelper(v3d.xCoord + this.pos.x - pos.xCoord, 0, v3d.zCoord + this.pos.y - pos.zCoord);
	}

	public Vec3D getCollisionPoint(Entity entity, float yaw) {
		Point prevPosP = new Point(entity.prevPosX - pos.x, entity.prevPosZ - pos.y);
		prevPosP = getZeroAxisPoint(prevPosP, yaw);

		Point posP = new Point(entity.posX - pos.x, entity.posZ - pos.y);
		posP = getZeroAxisPoint(posP, yaw);

		IntersectVector vector = new SimpleIntersectVector(prevPosP, posP);

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

		IntersectVector vector = new SimpleIntersectVector(prevPosP, posP);

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
		Point p = new Point(collideP, vector.getTo());
		Vec3D v3dU = Vec3D.createVectorHelper(v.x, 0, v.y);
		Vec3D v3dP = Vec3D.createVectorHelper(p.x, 0, p.y);
		double len = v3dP.dotProduct(v3dU);

		Point slideP = new Point(v3dU.xCoord * len, v3dU.zCoord * len);

		Point r = vector.getFrom().addedPoint(slideP);
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
		//
		IntersectResult result = new IntersectResult(null, null);
		Around.forEachTypeWork(result, new Around.OffsetTypeWorker<IntersectResultDto, Void>() {
			@Override
			public IntersectResultDto work(Around around, Void in, OffsetType offsetType) {
				IntersectResultDto out = new IntersectResultDto();
				switch (offsetType) {
				case POSITIVE:
					// 上(前)か左ならfromより大きくtoより小さい判定になる
					out.from = vector.getFrom();
					out.to = vector.getTo();
					break;
				case NEGATIVE:
					// 下(後)か右ならtoより大きくfromより小さい判定になる
					out.from = vector.getTo();
					out.to = vector.getFrom();
					break;
				}
				return out;
			}
		}, new Around.AxisTypeWorker<IntersectResultDto, IntersectResultDto>() {
			@Override
			public IntersectResultDto work(Around around, IntersectResultDto in, AxisType axisType) {
				IntersectVector v = vectorMap.get(around);
				if (v == null) {
					return null;
				}
				// ここでvの該当軸の値はfromでもtoでも同値(のはず)
				switch (axisType) {
				case Y:
					// Y軸タイプならYの値
					in.value = v.getFrom().y;
					in.min = in.from.y;
					in.max = in.to.y;
					break;
				case X:
					// X軸タイプならXの値
					in.value = v.getFrom().x;
					in.min = in.from.x;
					in.max = in.to.x;
					break;
				}
				return in;
			}
		}, new Around.TypeWorker<IntersectResult, IntersectResultDto>() {

			@Override
			public void work(Around around, IntersectResult dto, IntersectResultDto in) {
				if (in == null) {
					return;
				}
				if (dto.collideP != null) {
					return;// 既に衝突判定が出た
				}
				double value = in.value;
				double min = in.min;
				double max = in.max;

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
		EnumSupport.foreach(new AroundWorker.Each2SWorker<InitVectorDto, Around>() {
			@Override
			protected void afterWork(Around dir, InitVectorDto dto) {
				IntersectVector vector = new SimpleIntersectVector(dto.fX, dto.fY, dto.tX, dto.tY);
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
			protected void doInternalWorkNext(Direction dir, InitVectorDto dto) {
				Offset offset = dir.getOffset();
				double x = offsetMapX.get(offset.xIndex);
				dto.tX = x;

				double y = offsetMapY.get(offset.yIndex);
				dto.tY = y;
			}

			@Override
			protected void doInternalWorkPrev(Direction dir, InitVectorDto dto) {
				Offset offset = dir.getOffset();
				double x = offsetMapX.get(offset.xIndex);
				dto.fX = x;

				double y = offsetMapY.get(offset.yIndex);
				dto.fY = y;
			}

		}, new InitVectorDto(), Around.values());
	}

}

package net.minecraft.src.obb;

import net.minecraft.src.Entity;
import net.minecraft.src.MathHelper;
import net.minecraft.src.obb.AroundWorker.Around;

public class AbstractAngledTile implements AngledTile {

	protected static class EqualOrGreaterOperation implements Operation {
		@Override
		public boolean eval(double var1, double var2) {
			return var1 >= var2;
		}
	}

	protected static class EqualOrLessOperation implements Operation {
		@Override
		public boolean eval(double var1, double var2) {
			return var1 <= var2;
		}
	}

	protected static class IsInRectDto {
		boolean isIn;
	}

	protected interface Operation {
		boolean eval(double var1, double var2);
	}

	public static Point getZeroAxisPoint(Point point, float rad) {
		float f = MathHelper.cos(-rad);
		float f1 = MathHelper.sin(-rad);
		double d = point.x * f + point.y * f1;
		double d2 = point.y * f - point.x * f1;
		return new Point(d, d2);
	}

	protected Point from;
	protected Point to;
	protected Point pos;
	protected double y;

	public AbstractAngledTile() {
		super();
	}

	@Override
	public void fixHeightPosition(Entity entity, float yaw) {
	}

	public boolean isInRect(Entity entity, float yaw) {
		Point rP = new Point(entity.posX - pos.x, entity.posZ - pos.y);
		final Point posP = getZeroAxisPoint(rP, yaw);

		return isInRect(posP);
	}

	private boolean isInRect(final Point posP) {
		IsInRectDto result = new IsInRectDto();
		result.isIn = true;
		Around.foreach(new AroundWorker.DirectionalWorker<Around, IsInRectDto>() {
			@Override
			public void work(Around e, IsInRectDto dto) {
				if (!dto.isIn) {
					return;
				}
				Operation op = null;
				Point p = null;
				switch (e.getOffsetType()) {
				case NEGATIVE:
					p = from;
					op = new EqualOrGreaterOperation();
					break;
				case POSITIVE:
					p = to;
					op = new EqualOrLessOperation();
					break;
				default:
					new InternalError();
				}

				double range = 0;
				double value = 0;
				switch (e.getAxisType()) {
				case X:
					range = p.x;
					value = posP.x;
					break;
				case Y:
					range = p.y;
					value = posP.y;
					break;
				default:
					new InternalError();
				}

				dto.isIn = op.eval(value, range);
			}
		}, result);

		return result.isIn;
	}

}
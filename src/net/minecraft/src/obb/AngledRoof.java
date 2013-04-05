package net.minecraft.src.obb;

import net.minecraft.src.Entity;

public class AngledRoof extends AbstractAngledTile {

	public AngledRoof(Point from, Point to, Point pos, double y) {
		this.from = from;
		this.to = to;
		this.pos = pos;
		this.y = y;
	}

	@Override
	public void fixHeightPosition(Entity entity, float yaw) {
		double offset = entity.height;
		double offsetedY = y - offset;
		if (entity.posY > offsetedY && offsetedY >= entity.prevPosY) {
			entity.lastTickPosY = entity.prevPosY = offsetedY;
			entity.posY = entity.prevPosY + 0.00;
			if (entity.motionY > 0.000001) {
				entity.motionY = 0.000001;
			}
			entity.setPosition(entity.posX, entity.posY, entity.posZ);
		}
	}
}

package net.minecraft.src.obb;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;

public class AngledFloor extends AbstractAngledTile {

	public AngledFloor(Point from, Point to, Point pos, double y) {
		this.from = from;
		this.to = to;
		this.pos = pos;
		this.y = y + 0.09;
	}

	@Override
	public void fixHeightPosition(Entity entity, float yaw) {
		double bottomOffset = entity.posY - entity.boundingBox.minY - 0.1;
		double sneakOffset = entity.isSneaking() ? 0.1 : 0;
		if (entity.boundingBox.minY + sneakOffset < y && y <= entity.prevPosY) {
			// entity.lastTickPosY =
			entity.lastTickPosY = entity.prevPosY = entity.posY = y
					+ bottomOffset - sneakOffset;
			entity.onGround = true;
			System.out.println(entity.boundingBox.minY);
			if (entity instanceof EntityPlayer
					&& ((EntityPlayer) entity).capabilities.isFlying
					&& entity.isSneaking()) {
				entity.motionY = 0.1499999;
				if (!ModLoader.getMinecraftInstance().gameSettings.keyBindSneak.pressed) {
					entity.motionY = -0.000001;
				}
			} else if (entity.motionY < -0.14) {
				entity.motionY = -0.006;
			} else if (entity.motionY < -0.000001) {
				entity.motionY = -0.000001;
			}
			entity.setPosition(entity.posX, entity.posY, entity.posZ);
		}
	}
}

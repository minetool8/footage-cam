package com.footagecam;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Tilts the camera when you turn your head or move sideways. */
final class CameraRoll {
    private static boolean initialised;
    private static float prevYaw;
    private static float rotAmount;
    private static float spinRoll;
    private static float strafeRoll;

    private CameraRoll() {}

    static void reset() {
        initialised = false;
        rotAmount = 0.0F;
        spinRoll = 0.0F;
        strafeRoll = 0.0F;
    }

    /** @return roll in degrees to add to the camera */
    static float update(LocalPlayer player, float partialTick, float dt) {
        float yaw = player.getViewYRot(partialTick);
        if (!initialised) {
            prevYaw = yaw;
            initialised = true;
        }

        // Turning: collect how far the head turned, lean into it, let it settle.
        rotAmount += Mth.wrapDegrees(yaw - prevYaw);
        spinRoll = MathUtil.lerp(spinRoll, rotAmount * 0.1F * Config.LOOK_ROLL.get().floatValue(), 0.8F, dt);
        rotAmount = MathUtil.lerp(rotAmount, 0.0F, 0.5F, dt);
        spinRoll = Mth.clamp(spinRoll, -20.0F, 20.0F);

        // Strafing: sideways part of the velocity relative to where we look.
        Vec3 velocity = player.getDeltaMovement();
        float angle = (float) Math.toRadians(360.0F - player.getYRot());
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);
        float relativeX = (float) (velocity.x * cos - velocity.z * sin);
        strafeRoll = MathUtil.lerp(strafeRoll, -relativeX * 5.0F * Config.STRAFE_ROLL.get().floatValue(), 0.8F, dt);
        strafeRoll = Mth.clamp(strafeRoll, -20.0F, 20.0F);

        prevYaw = yaw;
        return spinRoll + strafeRoll;
    }
}

package com.footagecam;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/** Handheld camera sway. Always on a little, stronger and faster while moving. */
final class CameraShake {
    private static double trauma = 0.1D;
    private static double noiseSpeed = 0.1D;
    private static double noiseY = 0.0D;

    /** Offsets in degrees, valid after {@link #update}. */
    static float pitch;
    static float yaw;
    static float roll;

    private CameraShake() {}

    static void reset() {
        trauma = 0.1D;
        noiseSpeed = 0.1D;
        noiseY = 0.0D;
        pitch = 0.0F;
        yaw = 0.0F;
        roll = 0.0F;
    }

    static void update(LocalPlayer player, float dt) {
        if (noiseY >= 1000.0D) {
            noiseY = 0.0D;
        }

        float speed = (player.walkDist - player.walkDistO) * 6.0F;
        if (player.isFallFlying()) {
            speed /= 6.0F;
        }

        double traumaGoal = Mth.clamp(0.6D * speed, 0.5D, 1.5D);
        double noiseSpeedGoal = Mth.clamp(0.25D * speed, 0.1D, 1.0D);
        double amplitude = 4.0D;

        trauma = Math.max(MathUtil.lerp((float) trauma, (float) traumaGoal, 0.93F, dt), 0.5D);
        noiseSpeed = Math.max(MathUtil.lerp((float) noiseSpeed, (float) noiseSpeedGoal, 0.93F, dt), 0.1D);
        noiseY += noiseSpeed * dt;

        double intensity = trauma * trauma;
        pitch = (float) (amplitude * intensity * MathUtil.noise(1, noiseY));
        yaw = (float) (amplitude * intensity * MathUtil.noise(73, noiseY));
        roll = (float) (2.0D * amplitude * intensity * MathUtil.noise(146, noiseY));
    }
}

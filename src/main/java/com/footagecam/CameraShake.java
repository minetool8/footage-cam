package com.footagecam;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/** Handheld camera shake while walking and running. Standing still has none (see IdleSway for that). */
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

        // 0 while standing still, 1 as soon as the player walks. Walking and running keep exactly the
        // original SP-Backrooms numbers (trauma never below 0.5), standing still has no shake at all.
        double moving = Mth.clamp(speed / 0.3F, 0.0F, 1.0F);
        double floor = 0.5D * moving;
        double traumaGoal = Mth.clamp(0.6D * speed, floor, 1.5D);
        double noiseSpeedGoal = Mth.clamp(0.25D * speed, 0.1D, 1.0D);
        // SP-Backrooms uses 4 degrees; 35% softer here.
        double amplitude = 4.0D * 0.65D;

        // Fades out a little faster once the player has stopped.
        float traumaSmoothing = moving <= 0.0D ? 0.80F : 0.93F;
        trauma = Math.max(MathUtil.lerp((float) trauma, (float) traumaGoal, traumaSmoothing, dt), floor);
        noiseSpeed = Math.max(MathUtil.lerp((float) noiseSpeed, (float) noiseSpeedGoal, 0.93F, dt), 0.1D);
        noiseY += noiseSpeed * dt;

        double intensity = trauma * trauma;
        pitch = (float) (amplitude * intensity * MathUtil.noise(1, noiseY));
        yaw = (float) (amplitude * intensity * MathUtil.noise(73, noiseY));
        roll = (float) (2.0D * amplitude * intensity * MathUtil.noise(146, noiseY));
    }
}

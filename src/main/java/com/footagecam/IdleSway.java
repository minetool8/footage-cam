package com.footagecam;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.SimplexNoise;

/**
 * Slow cinematic sway while standing still, modelled on Camera Overhaul's "camera sway":
 * simplex noise on pitch and yaw that fades in once nothing has moved for a moment
 * and fades out again as soon as the player moves or turns.
 */
final class IdleSway {
    private static final double EPS = 1.0E-5D;

    private static boolean initialised;
    private static long lastNanos;
    private static double time;
    private static double lastActionTime;
    private static double factor;
    private static double target;
    private static float prevYaw;
    private static float prevPitch;

    /** Offsets in degrees, valid after {@link #update}. */
    static float pitch;
    static float yaw;

    private IdleSway() {}

    static void reset() {
        initialised = false;
        factor = 0.0D;
        target = 0.0D;
        pitch = 0.0F;
        yaw = 0.0F;
    }

    /**
     * Idle means: standing on the ground, no movement keys, not moving, not riding and not turning.
     * This looks at the player itself, not at the camera angles, because other mods change those every frame.
     */
    static void update(LocalPlayer player, boolean paused) {
        long now = System.nanoTime();
        double dt = 0.0D;
        if (initialised && !paused) {
            dt = Math.min((now - lastNanos) / 1.0E9D, 0.1D);
        }
        lastNanos = now;
        time += dt;

        float playerYaw = player.getYRot();
        float playerPitch = player.getXRot();
        if (!initialised) {
            prevYaw = playerYaw;
            prevPitch = playerPitch;
            lastActionTime = time;
            initialised = true;
        }

        Vec3 v = player.getDeltaMovement();
        boolean keys = player.input != null
                && (player.input.forwardImpulse != 0.0F || player.input.leftImpulse != 0.0F || player.input.jumping);
        boolean acted = keys
                || player.isPassenger()
                || !player.onGround()
                || v.x * v.x + v.z * v.z > 1.0E-6D
                || Math.abs(Mth.wrapDegrees(playerYaw - prevYaw)) > 1.0E-3F
                || Math.abs(playerPitch - prevPitch) > 1.0E-3F;
        if (acted) {
            lastActionTime = time;
        }
        prevYaw = playerYaw;
        prevPitch = playerPitch;

        // Same state machine as Camera Overhaul: after moving, fade out completely first,
        // then (once still for long enough) fade back in.
        if (time - lastActionTime < Config.IDLE_SWAY_DELAY.get()) {
            target = 0.0D;
        } else if (factor == target) {
            target = 1.0D;
        }

        double length = target > 0.0D ? Config.IDLE_SWAY_FADE_IN.get() : Config.IDLE_SWAY_FADE_OUT.get();
        double step = length > 0.0D ? dt / length : 1.0D;
        factor = MathUtil.stepTowards(factor, target, step);

        double intensity = Config.IDLE_SWAY_INTENSITY.get() * Math.pow(factor, 3.0D);
        float phase = (float) (time * Config.IDLE_SWAY_FREQUENCY.get());
        pitch = (float) (SimplexNoise.noise(phase, 420.0F) * intensity);
        yaw = (float) (SimplexNoise.noise(phase, 1337.0F) * intensity);
    }
}

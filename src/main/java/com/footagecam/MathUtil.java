package com.footagecam;

import net.minecraft.util.Mth;
import org.joml.SimplexNoise;

final class MathUtil {
    private MathUtil() {}

    /**
     * Frame-rate independent smoothing. {@code s} is the fraction of the gap that is
     * left after one tick (0 = jump straight to b), {@code dt} is the frame length in ticks.
     */
    static float lerp(float a, float b, float s, float dt) {
        return Mth.lerp((float) (1.0D - Math.pow(s, dt)), a, b);
    }

    /** Smooth simplex noise in [-1, 1], like SP-Backrooms. Each channel gives a different sequence. */
    static double noise(int channel, double x) {
        return SimplexNoise.noise((float) channel, (float) x);
    }

    /** Moves {@code current} toward {@code target} by at most {@code step}. */
    static double stepTowards(double current, double target, double step) {
        if (current < target) {
            return Math.min(current + step, target);
        }
        return Math.max(current - step, target);
    }
}

package com.footagecam;

import net.minecraft.util.Mth;

final class MathUtil {
    private MathUtil() {}

    /**
     * Frame-rate independent smoothing. {@code s} is the fraction of the gap that is
     * left after one tick (0 = jump straight to b), {@code dt} is the frame length in ticks.
     */
    static float lerp(float a, float b, float s, float dt) {
        return Mth.lerp((float) (1.0D - Math.pow(s, dt)), a, b);
    }

    /** Smooth 1D gradient noise, roughly in [-1, 1]. Each channel gives a different sequence. */
    static double noise(int channel, double x) {
        int xi = (int) Math.floor(x);
        double xf = x - xi;
        double g0 = gradient(channel, xi);
        double g1 = gradient(channel, xi + 1);
        double u = xf * xf * xf * (xf * (xf * 6.0D - 15.0D) + 10.0D);
        return 2.0D * Mth.lerp(u, g0 * xf, g1 * (xf - 1.0D));
    }

    private static double gradient(int channel, int i) {
        int h = channel * 0x27D4EB2F + i * 0x165667B1;
        h ^= h >>> 15;
        h *= 0x85EBCA6B;
        h ^= h >>> 13;
        h *= 0xC2B2AE35;
        h ^= h >>> 16;
        return (h & 0xFFFF) / 32767.5D - 1.0D;
    }
}

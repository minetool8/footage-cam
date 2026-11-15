package com.footagecam;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;

/**
 * Two layers of delay:
 *  1. the picture (camera) follows the real head rotation a moment later,
 *  2. the hands follow the picture a moment after that.
 */
final class ViewLag {
    private static final float MAX_HAND_OFFSET = 12.0F;

    private static boolean initialised;
    private static float camYaw;
    private static float camPitch;
    private static float handYaw;
    private static float handPitch;

    /** 0..1, how far the player is aiming down the sights of a gun (set every frame). */
    static float ads;

    /** Counts rendered frames so the hand offset is applied once per frame. */
    static long frameId;
    static long handAppliedFrame = -1L;

    private ViewLag() {}

    static void reset() {
        initialised = false;
        ads = 0.0F;
    }

    static void snap(float yaw, float pitch) {
        camYaw = handYaw = yaw;
        camPitch = handPitch = pitch;
        initialised = true;
    }

    static void update(float targetYaw, float targetPitch, float dt) {
        if (!initialised || Float.isNaN(camYaw) || Float.isNaN(camPitch)) {
            snap(targetYaw, targetPitch);
        }

        if (Config.ENABLE_SMOOTH.get()) {
            float smoothing = Config.SMOOTHING.get().floatValue() * (1.0F - ads * (1.0F - Config.ADS_SMOOTHING_MULT.get().floatValue()));
            float a = amount(smoothing, dt);
            camYaw += Mth.wrapDegrees(targetYaw - camYaw) * a;
            camPitch += (targetPitch - camPitch) * a;
        } else {
            camYaw = targetYaw;
            camPitch = targetPitch;
        }

        if (Config.ENABLE_HAND_LAG.get()) {
            float b = amount(Config.HAND_LAG_SMOOTHING.get().floatValue(), dt);
            handYaw += Mth.wrapDegrees(camYaw - handYaw) * b;
            handPitch += (camPitch - handPitch) * b;
        } else {
            handYaw = camYaw;
            handPitch = camPitch;
        }
    }

    static float camYaw() {
        return camYaw;
    }

    static float camPitch() {
        return camPitch;
    }

    /** Hand lag left over while aiming down the sights (1 = all of it, 0 = gun stays centred). */
    private static float handScale() {
        return 1.0F - ads * (1.0F - Config.ADS_HAND_LAG.get().floatValue());
    }

    /** How far (degrees) the hands trail the camera sideways. Positive = hands are turned left of the camera. */
    static float handOffsetYaw() {
        float strength = Config.HAND_LAG_STRENGTH.get().floatValue() * handScale();
        return Mth.clamp(Mth.wrapDegrees(camYaw - handYaw) * strength, -MAX_HAND_OFFSET, MAX_HAND_OFFSET);
    }

    /** How far (degrees) the hands trail the camera up/down. Positive = hands are turned up from the camera. */
    static float handOffsetPitch() {
        float strength = Config.HAND_LAG_STRENGTH.get().floatValue() * handScale();
        return Mth.clamp((camPitch - handPitch) * strength, -MAX_HAND_OFFSET, MAX_HAND_OFFSET);
    }

    /** Shifts the hand rendering so it trails behind the camera. */
    static void applyToHands(PoseStack poseStack) {
        poseStack.mulPose(Axis.YP.rotationDegrees(handOffsetYaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(handOffsetPitch()));
    }

    private static float amount(float s, float dt) {
        return (float) (1.0D - Math.pow(s, dt));
    }
}

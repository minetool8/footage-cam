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

    /** Counts rendered frames so the hand offset is applied once per frame. */
    static long frameId;
    static long handAppliedFrame = -1L;

    private ViewLag() {}

    static void reset() {
        initialised = false;
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
            float a = amount(Config.SMOOTHING.get().floatValue(), dt);
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

    /** Shifts the hand rendering so it trails behind the camera. */
    static void applyToHands(PoseStack poseStack) {
        float strength = Config.HAND_LAG_STRENGTH.get().floatValue();
        float dYaw = Mth.clamp(Mth.wrapDegrees(camYaw - handYaw) * strength, -MAX_HAND_OFFSET, MAX_HAND_OFFSET);
        float dPitch = Mth.clamp((camPitch - handPitch) * strength, -MAX_HAND_OFFSET, MAX_HAND_OFFSET);
        poseStack.mulPose(Axis.YP.rotationDegrees(dYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(dPitch));
    }

    private static float amount(float s, float dt) {
        return (float) (1.0D - Math.pow(s, dt));
    }
}

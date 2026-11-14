package com.footagecam;

import net.minecraftforge.common.ForgeConfigSpec;

/** Client config, written to config/footagecam-client.toml */
public final class Config {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_ROLL;
    public static final ForgeConfigSpec.DoubleValue LOOK_ROLL;
    public static final ForgeConfigSpec.DoubleValue STRAFE_ROLL;

    public static final ForgeConfigSpec.BooleanValue ENABLE_SHAKE;
    public static final ForgeConfigSpec.DoubleValue SHAKE_INTENSITY;

    public static final ForgeConfigSpec.BooleanValue ENABLE_SMOOTH;
    public static final ForgeConfigSpec.DoubleValue SMOOTHING;

    public static final ForgeConfigSpec.BooleanValue ENABLE_HAND_LAG;
    public static final ForgeConfigSpec.DoubleValue HAND_LAG_STRENGTH;
    public static final ForgeConfigSpec.DoubleValue HAND_LAG_SMOOTHING;

    public static final ForgeConfigSpec.BooleanValue MOTION_BLUR;
    public static final ForgeConfigSpec.DoubleValue MOTION_BLUR_STRENGTH;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("camera_roll");
        ENABLE_ROLL = b.comment("Tilt the camera when you turn and when you strafe.")
                .define("enabled", true);
        LOOK_ROLL = b.comment("How much the camera tilts when you turn your head.")
                .defineInRange("lookRollMultiplier", 4.0D, 0.0D, 30.0D);
        STRAFE_ROLL = b.comment("How much the camera tilts when you move sideways.")
                .defineInRange("strafeRollMultiplier", 4.0D, 0.0D, 30.0D);
        b.pop();

        b.push("camera_shake");
        ENABLE_SHAKE = b.comment("Constant handheld sway that gets a bit stronger while moving.")
                .define("enabled", true);
        SHAKE_INTENSITY = b.comment("Multiplier for the sway. 1.0 = default, 0 = none.")
                .defineInRange("intensity", 1.0D, 0.0D, 5.0D);
        b.pop();

        b.push("smooth_camera");
        ENABLE_SMOOTH = b.comment("The picture follows your mouse with a small delay (first person only).",
                        "Note: your crosshair/aim is where you really look, the picture catches up a moment later.")
                .define("enabled", true);
        SMOOTHING = b.comment("0 = instant, closer to 1 = more delay.")
                .defineInRange("smoothing", 0.7D, 0.0D, 0.99D);
        b.pop();

        b.push("hand_lag");
        ENABLE_HAND_LAG = b.comment("Hands and held items trail behind the camera when you turn.")
                .define("enabled", true);
        HAND_LAG_STRENGTH = b.comment("How far the hands drift while the camera turns.")
                .defineInRange("strength", 1.0D, 0.0D, 4.0D);
        HAND_LAG_SMOOTHING = b.comment("0 = hands never lag, closer to 1 = hands take longer to catch up.")
                .defineInRange("smoothing", 0.85D, 0.0D, 0.99D);
        b.pop();

        b.push("motion_blur");
        MOTION_BLUR = b.comment("Camera motion blur (turned off automatically with Iris/Oculus shader packs).")
                .define("enabled", true);
        MOTION_BLUR_STRENGTH = b.comment("Blur amount.")
                .defineInRange("strength", 0.5D, 0.0D, 2.0D);
        b.pop();

        SPEC = b.build();
    }

    private Config() {}
}

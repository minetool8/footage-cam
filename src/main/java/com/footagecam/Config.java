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

    public static final ForgeConfigSpec.BooleanValue ENABLE_IDLE_SWAY;
    public static final ForgeConfigSpec.DoubleValue IDLE_SWAY_INTENSITY;
    public static final ForgeConfigSpec.DoubleValue IDLE_SWAY_FREQUENCY;
    public static final ForgeConfigSpec.DoubleValue IDLE_SWAY_DELAY;
    public static final ForgeConfigSpec.DoubleValue IDLE_SWAY_FADE_IN;
    public static final ForgeConfigSpec.DoubleValue IDLE_SWAY_FADE_OUT;
    public static final ForgeConfigSpec.BooleanValue IDLE_SWAY_DEBUG;

    public static final ForgeConfigSpec.BooleanValue STOP_SWAY_IN_CONTAINERS;

    public static final ForgeConfigSpec.BooleanValue EXTRA_BOB;

    public static final ForgeConfigSpec.BooleanValue ENABLE_SMOOTH;
    public static final ForgeConfigSpec.DoubleValue SMOOTHING;

    public static final ForgeConfigSpec.BooleanValue ENABLE_HAND_LAG;
    public static final ForgeConfigSpec.DoubleValue HAND_LAG_STRENGTH;
    public static final ForgeConfigSpec.DoubleValue HAND_LAG_SMOOTHING;

    public static final ForgeConfigSpec.BooleanValue ADS_ENABLED;
    public static final ForgeConfigSpec.DoubleValue ADS_HAND_LAG;
    public static final ForgeConfigSpec.DoubleValue ADS_SWAY;
    public static final ForgeConfigSpec.DoubleValue ADS_TILT;
    public static final ForgeConfigSpec.DoubleValue ADS_SMOOTHING_MULT;
    public static final ForgeConfigSpec.BooleanValue ADS_ZOOM_SCALE;

    public static final ForgeConfigSpec.BooleanValue BULLETS_FOLLOW_GUN;
    public static final ForgeConfigSpec.BooleanValue GUN_AIM_INCLUDE_SWAY;

    public static final ForgeConfigSpec.BooleanValue MOTION_BLUR;
    public static final ForgeConfigSpec.DoubleValue MOTION_BLUR_STRENGTH;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("camera_roll");
        ENABLE_ROLL = b.comment("Tilt the camera when you turn and when you strafe.")
                .define("enabled", true);
        LOOK_ROLL = b.comment("How much the camera tilts when you turn your head. (SP-Backrooms default: 7, halved here)")
                .defineInRange("lookRollMultiplier", 3.5D, 0.0D, 30.0D);
        STRAFE_ROLL = b.comment("How much the camera tilts when you move sideways. (SP-Backrooms default: 7, halved here)")
                .defineInRange("strafeRollMultiplier", 3.5D, 0.0D, 30.0D);
        b.pop();

        b.push("camera_shake");
        ENABLE_SHAKE = b.comment("Handheld shake while walking and running (35% softer than SP-Backrooms).",
                        "There is none while standing still.")
                .define("enabled", true);
        SHAKE_INTENSITY = b.comment("Multiplier for the sway. 1.0 = default, 0 = none.")
                .defineInRange("intensity", 1.0D, 0.0D, 5.0D);
        b.pop();

        b.push("idle_sway");
        ENABLE_IDLE_SWAY = b.comment("Very slow, very small camera sway while you stand still (Camera Overhaul style).",
                        "It fades out as soon as you move or turn.")
                .define("enabled", true);
        IDLE_SWAY_INTENSITY = b.comment("How far the camera drifts, in degrees. Camera Overhaul default: 0.6")
                .defineInRange("amountDegrees", 0.9D, 0.0D, 5.0D);
        IDLE_SWAY_FREQUENCY = b.comment("Sway speed. Camera Overhaul default: 0.16. Below about 0.15 it is too slow to notice.")
                .defineInRange("speed", 0.3D, 0.0D, 2.0D);
        IDLE_SWAY_DELAY = b.comment("Seconds you must stand still before the sway starts.",
                        "Camera Overhaul default is 0.15, here it is shorter so it feels instant.")
                .defineInRange("startDelaySeconds", 0.05D, 0.01D, 10.0D);
        IDLE_SWAY_FADE_IN = b.comment("Seconds the sway takes to reach full strength. 0 = jumps in at once.",
                        "Camera Overhaul default is 5.0 (a very slow build-up).")
                .defineInRange("fadeInSeconds", 0.1D, 0.0D, 30.0D);
        IDLE_SWAY_FADE_OUT = b.comment("Seconds the sway takes to disappear when you start moving.")
                .defineInRange("fadeOutSeconds", 0.1D, 0.0D, 30.0D);
        IDLE_SWAY_DEBUG = b.comment("Shows on the action bar why the idle sway is running or what is blocking it.")
                .define("debugOverlay", false);
        b.pop();

        b.push("menus");
        STOP_SWAY_IN_CONTAINERS = b.comment("All camera sway, shake and tilt fade out while an inventory, chest or any other",
                        "container screen is open, and fade back in when it closes.")
                .define("stopSwayInContainers", true);
        b.pop();

        b.push("camera_bob");
        EXTRA_BOB = b.comment("SP-Backrooms makes the walking head bob tip the view up and down twice as much as vanilla.",
                        "Only has an effect while 'View Bobbing' is on in the video settings.")
                .define("strongerPitchBob", true);
        b.pop();

        b.push("smooth_camera");
        ENABLE_SMOOTH = b.comment("The picture follows your mouse with a small delay (first person only).",
                        "Note: your real aim is where you really look, the picture catches up a moment later.")
                .define("enabled", true);
        SMOOTHING = b.comment("0 = instant, closer to 1 = more delay. (SP-Backrooms default: 0.7)")
                .defineInRange("smoothing", 0.7D, 0.0D, 0.99D);
        b.pop();

        b.push("hand_lag");
        ENABLE_HAND_LAG = b.comment("Hands, held items and TACZ guns trail behind the camera when you turn.")
                .define("enabled", true);
        HAND_LAG_STRENGTH = b.comment("How far the hands drift while the camera turns.")
                .defineInRange("strength", 1.0D, 0.0D, 4.0D);
        HAND_LAG_SMOOTHING = b.comment("0 = hands never lag, closer to 1 = hands take longer to catch up.")
                .defineInRange("smoothing", 0.85D, 0.0D, 0.99D);
        b.pop();

        b.push("aim_down_sights");
        ADS_ENABLED = b.comment("While aiming a TACZ gun (right click), the gun stays in the middle of the screen and",
                        "the camera calms down, so you can actually hit things. Everything below blends in smoothly",
                        "as the gun comes up and blends back out when you lower it.")
                .define("enabled", true);
        ADS_HAND_LAG = b.comment("How much of the hand lag is left while aiming. 0 = gun stays exactly in the middle.")
                .defineInRange("handLagMultiplier", 0.0D, 0.0D, 1.0D);
        ADS_SWAY = b.comment("How much of the camera sway/shake is left while aiming. 1 = full, 0 = none.",
                        "The camera still moves, just less.")
                .defineInRange("swayMultiplier", 0.35D, 0.0D, 1.0D);
        ADS_TILT = b.comment("How much of the turn/strafe tilt is left while aiming.")
                .defineInRange("tiltMultiplier", 0.3D, 0.0D, 1.0D);
        ADS_SMOOTHING_MULT = b.comment("Camera follow delay while aiming, as a fraction of the normal delay.",
                        "Lower = the picture keeps up with your mouse better when you aim.")
                .defineInRange("cameraDelayMultiplier", 0.5D, 0.0D, 1.0D);
        ADS_ZOOM_SCALE = b.comment("Scale the sway down further when the scope zooms in, so a 4x scope does not look",
                        "4x as shaky. The size of the wobble on screen then stays about the same.")
                .define("scaleSwayWithZoom", true);
        b.pop();

        b.push("gun_aim");
        BULLETS_FOLLOW_GUN = b.comment("While holding a TACZ gun, shots leave where the front of the gun really points",
                        "(the delayed gun you see), not where your mouse aim is. Turn off for vanilla TACZ aiming.")
                .define("bulletsFollowGun", true);
        GUN_AIM_INCLUDE_SWAY = b.comment("Also include the sway/shake of the picture in the shot direction.",
                        "On: bullets follow exactly what you see. Off: only the camera lag and hand lag count.")
                .define("includeSway", true);
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

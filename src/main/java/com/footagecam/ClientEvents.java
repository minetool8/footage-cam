package com.footagecam;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FootageCam.MODID, value = Dist.CLIENT)
public final class ClientEvents {
    /** Latest world FOV (including TACZ's scope zoom), used to calm the sway down when zoomed in. */
    private static double lastFov = -1.0D;

    /** 0..1, how far the sway has faded out because a container screen is open. */
    private static float menuFade;

    private ClientEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (event.usedConfiguredFov()) {
            lastFov = event.getFOV();
        }
    }

    /** Runs once per rendered frame, right before the world is drawn. */
    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        // The shot-direction swap (see GunAim) must never leak into rendering.
        GunAim.restore(player);

        boolean paused = mc.isPaused();
        float dt = paused ? 0.0F : mc.getDeltaFrameTime();
        float partialTick = (float) event.getPartialTick();
        ViewLag.frameId++;

        float rawYaw = event.getYaw();
        float rawPitch = event.getPitch();
        float yaw = rawYaw;
        float pitch = rawPitch;
        float roll = event.getRoll();

        boolean ownCamera = mc.getCameraEntity() == player;
        boolean firstPerson = mc.options.getCameraType().isFirstPerson();

        // Aiming down the sights: how far the gun is up (0..1). Everything below blends with this.
        boolean holdingGun = ownCamera && firstPerson && GunAim.isHoldingGun(player);
        if (holdingGun) {
            GunAim.ensureShotHook();
        }
        float ads = 0.0F;
        if (holdingGun && Config.ADS_ENABLED.get()) {
            ads = GunAim.aimingProgress(player, partialTick, dt);
        }
        ViewLag.ads = ads;
        float swayScale = 1.0F - ads * (1.0F - Config.ADS_SWAY.get().floatValue());
        float tiltScale = 1.0F - ads * (1.0F - Config.ADS_TILT.get().floatValue());
        if (ads > 0.0F && Config.ADS_ZOOM_SCALE.get() && lastFov > 0.0D) {
            float zoom = Mth.clamp((float) (lastFov / mc.options.fov().get()), 0.1F, 1.0F);
            swayScale *= 1.0F - ads * (1.0F - zoom);
        }

        // Inventory, chests and other containers: all sway fades out and comes back when it closes.
        boolean containerOpen = Config.STOP_SWAY_IN_CONTAINERS.get() && mc.screen instanceof AbstractContainerScreen<?>;
        menuFade = (float) MathUtil.stepTowards(menuFade, containerOpen ? 1.0D : 0.0D, dt / 4.0F);
        swayScale *= 1.0F - menuFade;
        tiltScale *= 1.0F - menuFade;

        // 1. The picture follows the mouse a moment later, then the hands follow the picture.
        if (ownCamera && firstPerson) {
            ViewLag.update(rawYaw, rawPitch, dt);
            if (Config.ENABLE_SMOOTH.get()) {
                yaw = ViewLag.camYaw();
                pitch = ViewLag.camPitch();
            }
        } else {
            ViewLag.snap(rawYaw, rawPitch);
        }

        // 2. Sway on top of that (everything here moves the gun with the picture).
        float swayYaw = 0.0F;
        float swayPitch = 0.0F;

        if (ownCamera) {
            if (Config.ENABLE_ROLL.get()) {
                roll += CameraRoll.update(player, partialTick, dt) * tiltScale;
            } else {
                CameraRoll.reset();
            }

            if (Config.ENABLE_SHAKE.get()) {
                CameraShake.update(player, dt);
                float k = Config.SHAKE_INTENSITY.get().floatValue();
                swayPitch += CameraShake.pitch * k * swayScale;
                swayYaw += CameraShake.yaw * k * swayScale;
                roll += CameraShake.roll * k * swayScale;
            }

            if (Config.ENABLE_IDLE_SWAY.get()) {
                IdleSway.update(player, paused);
                if (Config.IDLE_SWAY_DEBUG.get()) {
                    player.displayClientMessage(Component.literal(IdleSway.debugText()), true);
                }
                swayPitch += IdleSway.pitch * swayScale;
                swayYaw += IdleSway.yaw * swayScale;
            } else {
                IdleSway.reset();
            }

            // SP-Backrooms doubles the pitch part of vanilla's walking view bob (5 -> 10 degrees),
            // so the extra half is added here.
            if (Config.EXTRA_BOB.get() && mc.options.bobView().get()) {
                float walked = player.walkDist - player.walkDistO;
                float f1 = -(player.walkDist + walked * partialTick);
                float f2 = Mth.lerp(partialTick, player.oBob, player.bob);
                swayPitch += Math.abs(Mth.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F * swayScale;
            }
        }

        yaw += swayYaw;
        pitch += swayPitch;

        // 3. Where the front of the gun really points: the picture minus how far the gun trails it.
        if (holdingGun && Config.BULLETS_FOLLOW_GUN.get()) {
            boolean withSway = Config.GUN_AIM_INCLUDE_SWAY.get();
            GunAim.record((withSway ? yaw : yaw - swayYaw) - ViewLag.handOffsetYaw(),
                    (withSway ? pitch : pitch - swayPitch) - ViewLag.handOffsetPitch());
        } else {
            GunAim.invalidate();
        }

        event.setYaw(yaw);
        event.setPitch(pitch);
        event.setRoll(roll);
    }

    /**
     * Both hands share one pose stack, so the offset is applied the first time only.
     * Runs first: TACZ draws its guns in this event and then cancels it, so the offset has to be
     * on the pose stack before TACZ reads it.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderHand(RenderHandEvent event) {
        if (!Config.ENABLE_HAND_LAG.get() || ViewLag.handAppliedFrame == ViewLag.frameId) {
            return;
        }
        ViewLag.handAppliedFrame = ViewLag.frameId;
        ViewLag.applyToHands(event.getPoseStack());
    }

    /** End of the local player's tick: right before its rotation goes out to the server. */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (event.player == mc.player && mc.player != null) {
            GunAim.swapIn(mc.player);
        }
    }

    /** End of the client tick: the real rotation is back before anything else looks at it. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            GunAim.restore(Minecraft.getInstance().player);
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            MotionBlur.run(event.getPoseStack(), event.getProjectionMatrix(), event.getCamera(), event.getPartialTick());
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        GunAim.restore(Minecraft.getInstance().player);
        GunAim.reset();
        menuFade = 0.0F;
        IdleSway.reset();
        CameraRoll.reset();
        CameraShake.reset();
        ViewLag.reset();
        MotionBlur.resetHistory();
    }
}

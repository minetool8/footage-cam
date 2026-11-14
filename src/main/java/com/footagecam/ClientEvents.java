package com.footagecam;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FootageCam.MODID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {}

    /** Runs once per rendered frame, right before the world is drawn. */
    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        float dt = mc.isPaused() ? 0.0F : mc.getDeltaFrameTime();
        float partialTick = (float) event.getPartialTick();
        ViewLag.frameId++;

        float yaw = event.getYaw();
        float pitch = event.getPitch();
        float roll = event.getRoll();

        boolean ownCamera = mc.getCameraEntity() == player;
        boolean firstPerson = mc.options.getCameraType().isFirstPerson();

        if (ownCamera && firstPerson) {
            ViewLag.update(yaw, pitch, dt);
            if (Config.ENABLE_SMOOTH.get()) {
                yaw = ViewLag.camYaw();
                pitch = ViewLag.camPitch();
            }
        } else {
            ViewLag.snap(yaw, pitch);
        }

        if (ownCamera) {
            if (Config.ENABLE_ROLL.get()) {
                roll += CameraRoll.update(player, partialTick, dt);
            } else {
                CameraRoll.reset();
            }

            if (Config.ENABLE_SHAKE.get()) {
                CameraShake.update(player, dt);
                float k = Config.SHAKE_INTENSITY.get().floatValue();
                pitch += CameraShake.pitch * k;
                yaw += CameraShake.yaw * k;
                roll += CameraShake.roll * k;
            }
        }

        event.setYaw(yaw);
        event.setPitch(pitch);
        event.setRoll(roll);
    }

    /** Both hands share one pose stack, so the offset is applied the first time only. */
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (!Config.ENABLE_HAND_LAG.get() || ViewLag.handAppliedFrame == ViewLag.frameId) {
            return;
        }
        ViewLag.handAppliedFrame = ViewLag.frameId;
        ViewLag.applyToHands(event.getPoseStack());
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            MotionBlur.run(event.getPoseStack(), event.getProjectionMatrix(), event.getCamera(), event.getPartialTick());
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CameraRoll.reset();
        CameraShake.reset();
        ViewLag.reset();
        MotionBlur.resetHistory();
    }
}

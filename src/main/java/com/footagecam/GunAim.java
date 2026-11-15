package com.footagecam;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Makes TACZ shots leave where the front of the (delayed) gun points.
 *
 * TACZ fires bullets along the player's rotation as the server knows it, and the server only
 * knows what the client sends in its movement packets once per tick. Right after the player
 * entity has ticked, and right before that packet is built, the rotation is swapped for the
 * direction of the gun barrel (camera + sway minus the hand lag, worked out in the last frame).
 * At the end of the tick the real rotation is put back, so mouse look and movement never see it.
 *
 * No TACZ classes are used, the item is recognised by its registry name.
 */
final class GunAim {
    private static final String GUN_NAMESPACE = "tacz";
    private static final String GUN_PATH = "modern_kinetic_gun";

    private static boolean valid;
    private static float gunYaw;
    private static float gunPitch;

    private static boolean swapped;
    private static float savedYaw;
    private static float savedPitch;

    // TACZ's own "how far are the sights up" value, looked up by reflection so TACZ is not needed to build.
    private static boolean lookedUp;
    private static Method fromLocalPlayer;
    private static Method getAimingProgress;
    private static float fallbackProgress;

    private GunAim() {}

    static boolean isHoldingGun(LocalPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && GUN_NAMESPACE.equals(id.getNamespace()) && GUN_PATH.equals(id.getPath());
    }

    /**
     * 0..1: how far the player has the gun up to the sights. Uses TACZ's own value, and falls back to the
     * right mouse button if that cannot be read.
     */
    static float aimingProgress(LocalPlayer player, float partialTick, float dt) {
        if (!lookedUp) {
            lookedUp = true;
            try {
                Class<?> operator = Class.forName("com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator");
                fromLocalPlayer = operator.getMethod("fromLocalPlayer", LocalPlayer.class);
                getAimingProgress = operator.getMethod("getClientAimingProgress", float.class);
            } catch (Throwable t) {
                fromLocalPlayer = null;
                getAimingProgress = null;
            }
        }
        if (fromLocalPlayer != null) {
            try {
                Object operator = fromLocalPlayer.invoke(null, player);
                Object progress = getAimingProgress.invoke(operator, partialTick);
                if (progress instanceof Float f) {
                    return Mth.clamp(f, 0.0F, 1.0F);
                }
            } catch (Throwable t) {
                fromLocalPlayer = null;
            }
        }
        boolean down = Minecraft.getInstance().options.keyUse.isDown();
        fallbackProgress = (float) MathUtil.stepTowards(fallbackProgress, down ? 1.0D : 0.0D, 0.2D * dt);
        return fallbackProgress;
    }

    /** Called every frame with the direction the gun barrel points in (degrees). */
    static void record(float yaw, float pitch) {
        gunYaw = yaw;
        gunPitch = pitch;
        valid = true;
    }

    /** Called every frame when no gun direction applies (no gun, third person, feature off). */
    static void invalidate() {
        valid = false;
    }

    static void reset() {
        valid = false;
        swapped = false;
    }

    /** Runs after the local player ticked and before its rotation is sent to the server. */
    static void swapIn(LocalPlayer player) {
        restore(player);
        if (!valid || !Config.BULLETS_FOLLOW_GUN.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.getCameraEntity() != player || !mc.options.getCameraType().isFirstPerson() || !isHoldingGun(player)) {
            return;
        }
        savedYaw = player.getYRot();
        savedPitch = player.getXRot();
        player.setYRot(savedYaw + Mth.wrapDegrees(gunYaw - savedYaw));
        player.setXRot(Mth.clamp(gunPitch, -90.0F, 90.0F));
        swapped = true;
    }

    /** Puts the real mouse-look rotation back. Safe to call at any time. */
    static void restore(LocalPlayer player) {
        if (!swapped) {
            return;
        }
        swapped = false;
        if (player != null) {
            player.setYRot(savedYaw);
            player.setXRot(savedPitch);
        }
    }
}

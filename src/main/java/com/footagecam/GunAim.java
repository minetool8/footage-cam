package com.footagecam;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Detects when the player is holding a TACZ gun.
 *
 * TACZ fires bullets along the player's real look direction (the mouse aim), not along
 * the picture. While the camera lags, sways or the hands trail behind, the gun on screen
 * points somewhere else than the bullet goes. To keep the gun and the shot together the
 * camera effects that move the aim direction are switched off while a gun is held.
 * No TACZ classes are used, the item is recognised by its registry name.
 */
final class GunAim {
    private static final String GUN_NAMESPACE = "tacz";
    private static final String GUN_PATH = "modern_kinetic_gun";

    private GunAim() {}

    static boolean isHoldingGun(LocalPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && GUN_NAMESPACE.equals(id.getNamespace()) && GUN_PATH.equals(id.getPath());
    }
}

package com.footagecam;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Footage Cam - a found-footage style first person camera for Minecraft Forge 1.20.1.
 *
 * Camera behaviour modelled on SP-Backrooms Revamped by SpacePotato and Herr Chaos.
 * Licensed GPL-3.0-only, see LICENSE and NOTICE.
 */
@Mod(FootageCam.MODID)
public class FootageCam {
    public static final String MODID = "footagecam";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FootageCam() {
        // Client-only mod: do not complain when joining servers that do not have it.
        ModLoadingContext.get().registerExtensionPoint(
                IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        () -> IExtensionPoint.DisplayTest.IGNORESERVERONLY,
                        (remote, isServer) -> true));

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }
}

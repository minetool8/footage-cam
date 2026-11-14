package com.footagecam;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import org.joml.Matrix4f;

/**
 * Camera motion blur. The world is rendered, then every pixel is reprojected into the previous
 * frame's camera (using the depth buffer) and the picture is smeared along that movement.
 * Runs before the hands are drawn, so hands never get blurred.
 */
final class MotionBlur {
    private static final ResourceLocation POST = new ResourceLocation(FootageCam.MODID, "shaders/post/motion_blur.json");
    private static final String[] MATRIX_UNIFORMS = {
            "M00", "M01", "M02", "M03",
            "M10", "M11", "M12", "M13",
            "M20", "M21", "M22", "M23",
            "M30", "M31", "M32", "M33"
    };

    private static PostChain chain;
    private static boolean failed;
    private static int width = -1;
    private static int height = -1;

    private static final Matrix4f PREV_VIEW = new Matrix4f();
    private static final Matrix4f PREV_PROJ = new Matrix4f();
    private static Vec3 prevCamera;
    private static boolean hasPrevious;

    private static final boolean SHADER_PACK_MOD =
            ModList.get().isLoaded("oculus") || ModList.get().isLoaded("iris");

    private MotionBlur() {}

    static void resetHistory() {
        hasPrevious = false;
    }

    /** Called after resource reloads and when leaving the game. */
    static void invalidate() {
        if (chain != null) {
            chain.close();
            chain = null;
        }
        failed = false;
        width = -1;
        height = -1;
        hasPrevious = false;
    }

    static void run(PoseStack poseStack, Matrix4f projection, Camera camera, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (!Config.MOTION_BLUR.get() || SHADER_PACK_MOD || mc.player == null || mc.level == null) {
            hasPrevious = false;
            return;
        }

        Matrix4f view = new Matrix4f(poseStack.last().pose());
        Matrix4f proj = new Matrix4f(projection);
        Vec3 cameraPos = camera.getPosition();

        boolean teleported = prevCamera != null && cameraPos.distanceToSqr(prevCamera) > 16.0D * 16.0D;
        if (hasPrevious && !teleported && !mc.isPaused() && ensureChain(mc)) {
            // previous clip space <- previous view <- (camera movement) <- current view <- current clip space
            Matrix4f reproject = new Matrix4f(PREV_PROJ)
                    .mul(PREV_VIEW)
                    .translate((float) (cameraPos.x - prevCamera.x),
                            (float) (cameraPos.y - prevCamera.y),
                            (float) (cameraPos.z - prevCamera.z))
                    .mul(new Matrix4f(view).invert())
                    .mul(new Matrix4f(proj).invert());

            float[] values = new float[16];
            reproject.get(values);
            for (int i = 0; i < 16; i++) {
                chain.setUniform(MATRIX_UNIFORMS[i], values[i]);
            }
            chain.setUniform("Strength", Config.MOTION_BLUR_STRENGTH.get().floatValue());

            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            chain.process(partialTick);
            mc.getMainRenderTarget().bindWrite(true);
            RenderSystem.enableDepthTest();
        }

        PREV_VIEW.set(view);
        PREV_PROJ.set(proj);
        prevCamera = cameraPos;
        hasPrevious = true;
    }

    private static boolean ensureChain(Minecraft mc) {
        if (failed) {
            return false;
        }
        Window window = mc.getWindow();
        try {
            if (chain == null) {
                chain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), POST);
                width = -1;
            }
            if (width != window.getWidth() || height != window.getHeight()) {
                chain.resize(window.getWidth(), window.getHeight());
                width = window.getWidth();
                height = window.getHeight();
            }
            return true;
        } catch (Exception e) {
            FootageCam.LOGGER.error("Could not load the motion blur shader, motion blur is disabled until resources reload", e);
            failed = true;
            if (chain != null) {
                chain.close();
                chain = null;
            }
            return false;
        }
    }
}

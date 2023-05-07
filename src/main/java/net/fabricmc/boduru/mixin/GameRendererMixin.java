package net.fabricmc.boduru.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.boduru.main.WaterShaderMod;
import net.fabricmc.boduru.shading.Framebuffers;
import net.fabricmc.boduru.shading.RenderPass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    //@Inject(at = @At("HEAD"), method = "render")
    private void renderHead(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        if (!WaterShaderMod.renderPass.doDrawWater()) {
            MinecraftClient client = MinecraftClient.getInstance();

            Entity camera = client.player;
            if (camera != null) {
                Vec3d position = camera.getPos();
                float waterHeight = WaterShaderMod.clipPlane.getHeight();

//                double d = 2 * (position.getY() - waterHeight);
//                camera.setPos(position.getX(), 100, position.getZ());

//                Camera cameragr = client.gameRenderer.getCamera();
//                camera.update(client.world, client.getCameraEntity() == null ? client.player : client.getCameraEntity(), !client.options.getPerspective().isFirstPerson(), client.options.getPerspective().isFrontView(), tickDelta);
//                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
//                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
            }
        }
    }

    //@Inject(at = @At("TAIL"), method = "render")
    private void renderTail(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        if (!WaterShaderMod.renderPass.doDrawWater()) {
            MinecraftClient client = MinecraftClient.getInstance();

            Entity camera = client.player;
            if (camera != null) {
                Vec3d position = camera.getPos();
                float waterHeight = WaterShaderMod.clipPlane.getHeight();

                double d = 2 * (position.getY() - waterHeight);
//                camera.setPos(position.getX(), 90, position.getZ());

                Camera cameragr = client.gameRenderer.getCamera();
                ((CameraMixin) cameragr).invokeSetPos(position.getX(), 90, position.getZ());
            }
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V"))
    private void renderWorld(GameRenderer instance, float tickDelta, long limitTime, MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();

        int reflectionFBO = WaterShaderMod.framebuffers.getReflectionFBO();
        int refractionFBO = WaterShaderMod.framebuffers.getRefractionFBO();
        int minecraftFBO = client.getFramebuffer().fbo;
        int reflectionColorTexture = WaterShaderMod.framebuffers.getReflectionTexture();

        float waterHeight = WaterShaderMod.clipPlane.getHeight();

        if (client.player != null) {
            Entity camera = client.player;
//            Camera camera = client.gameRenderer.getCamera();
            Vec3d position = camera.getPos();
            float pitch = camera.getPitch();

            // Setup reflection texture render pass
            double d = 2 * (position.getY() - waterHeight);
//            ((CameraMixin) camera).invokeMoveBy(0, -d, 0);
//            camera.setPos(position.getX(), position.getY() - d, position.getZ());
//            camera.setPitch(-pitch);

            // Set clipping plane to cull everything below the water
            Vector4f plane = new Vector4f(0.0f, 1.0f, 0.0f, -waterHeight);
            WaterShaderMod.vanillaShaders.setupVanillaShadersClippingPlanes(client, camera, plane);

            // Render reflection texture
            WaterShaderMod.renderPass.setDrawWater(false);
            instance.renderWorld(tickDelta, limitTime, matrices);

//            Framebuffers.CopyFrameBufferTexture(width, height, 0, reflectionFBO);
//            Framebuffers.CopyFrameBufferTexture(width, height, reflectionFBO, minecraftFBO);
//            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

            if (Framebuffers.frameCount % 100 == 0) {
//                Framebuffers.CopyFrameBufferTexture(width, height, 0, reflectionFBO);
//                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, reflectionFBO);
//                System.out.println("Frame count: " + Framebuffers.frameCount);
//                Framebuffers.SaveImage(width, height);
//                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
            }

//            WaterShaderMod.framebuffers.bindReflectionFrameBuffer();
//            WaterShaderMod.vanillaShaders.setupWaterShaderTexture(client, reflectionColorTexture);
//            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, minecraftFBO);
//
//            // Restore pitch and position
//            ((CameraMixin) camera).invokeSetPos(position.getX(), position.getY(), position.getZ());
//            ((CameraMixin) camera).invokeMoveBy(0, d, 0);
//            camera.setPos(position.getX(), position.getY(), position.getZ());
//            camera.setPitch(pitch);
//
//            // Set clipping plane to cull nothing
//            plane = new Vector4f(0.0f, -1.0f, 0.0f, 512f);
//            WaterShaderMod.vanillaShaders.setupVanillaShadersClippingPlanes(client, camera, plane);
//
//            // Restore pitch and render world
//            instance.renderWorld(tickDelta, limitTime, new MatrixStack());
//            WaterShaderMod.renderPass.setDrawWater(true);
//            instance.renderWorld(tickDelta, limitTime, new MatrixStack());

//            WaterShaderMod.screenQuad.render(reflectionColorTexture);

            Framebuffers.frameCount++;
        }
    }
}

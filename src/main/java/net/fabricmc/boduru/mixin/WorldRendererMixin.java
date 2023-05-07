package net.fabricmc.boduru.mixin;

import net.fabricmc.boduru.main.WaterShaderMod;
import net.fabricmc.boduru.shading.Framebuffers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow
    private Framebuffer entityOutlinesFramebuffer;
    @Shadow
    private Framebuffer entityFramebuffer;
    @Shadow
    private Framebuffer particlesFramebuffer;
    @Shadow
    private Framebuffer weatherFramebuffer;
    @Shadow
    private Framebuffer translucentFramebuffer;
    @Shadow
    private Framebuffer cloudsFramebuffer;

    //@Inject(at = @At("HEAD"), method = "render")
    private void renderHead(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        if (!WaterShaderMod.renderPass.doDrawWater()) {
            MinecraftClient client = MinecraftClient.getInstance();
            Entity cameraclient = client.player;
            Vec3d position = cameraclient.getPos();
            cameraclient.setPos(position.x, position.y - 10, position.z);

            float waterHeight = WaterShaderMod.clipPlane.getHeight();
            double d = 2 * (position.getY() - waterHeight);
            ((CameraMixin) camera).invokeSetPos(position.x, position.y, position.z);
        }
    }

    //@Inject(at = @At("TAIL"), method = "render")
    private void renderTail(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        if (!WaterShaderMod.renderPass.doDrawWater()) {
            MinecraftClient client = MinecraftClient.getInstance();
            Entity cameraclient = client.player;
            Vec3d position = cameraclient.getPos();
            cameraclient.setPos(position.x, position.y + 10, position.z);

            float waterHeight = WaterShaderMod.clipPlane.getHeight();
            double d = 2 * (position.getY() - waterHeight);
            ((CameraMixin) camera).invokeSetPos(position.x, position.y, position.z);
        }
    }
//
//    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getFramebuffer()Lnet/minecraft/client/gl/Framebuffer;", shift = At.Shift.AFTER), method = "render")
//    private void render2(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
//        if (!WaterShaderMod.renderPass.doDrawWater()) {
//            WaterShaderMod.framebuffers.bindReflectionFrameBuffer();
//        }
//    }

    @Inject(at = @At("TAIL"), method = "render")
    private void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        if (!WaterShaderMod.renderPass.doDrawWater()) {
            MinecraftClient client = MinecraftClient.getInstance();
            int width = client.getWindow().getFramebufferWidth();
            int height = client.getWindow().getFramebufferHeight();
            int minecraftFBO = client.getFramebuffer().fbo;
            int reflectionFBO = WaterShaderMod.framebuffers.getReflectionFBO();

            Framebuffers.CopyFrameBufferTexture(width, height, minecraftFBO, reflectionFBO);

            // Fill the reflection framebuffer with lightblue color
//            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, reflectionFBO);
////            GL11.glClearColor(0.5f, 0.5f, 1.0f, 1.0f);
//            GL11.glClearColor(1.0f, 0.3f, 0.0f, 1.0f);
//            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
//            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
//            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        }
    }

    @Shadow
    private void renderLayer(RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix) {
    }

    @Redirect(method = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDDLorg/joml/Matrix4f;)V", ordinal = 5))
    private void redirectWaterDrawCall(WorldRenderer instance, RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix) {
        if (WaterShaderMod.renderPass.doDrawWater()) {
            renderLayer(renderLayer, matrices, cameraX, cameraY, cameraZ, positionMatrix);
        }
    }

//    @Inject(at = @At(value = "INVOKE", target = ""), method = "renderLayer")
//    private void renderLayerCall() {
//
//    }
}

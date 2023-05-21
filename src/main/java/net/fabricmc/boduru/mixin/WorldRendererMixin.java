package net.fabricmc.boduru.mixin;

import net.fabricmc.boduru.main.WaterShaderMod;
import net.fabricmc.boduru.shading.Framebuffers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(at = @At("HEAD"), method = "render")
    private void renderHead(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null) {
            if (!WaterShaderMod.renderPass.doDrawWater()) {
                float waterHeight = WaterShaderMod.clipPlane.getHeight();
                Vector4f plane = new Vector4f(0.0f, 1.0f, 0.0f, -waterHeight);
                WaterShaderMod.vanillaShaders.setupVanillaShadersClippingPlanes(client, client.player, plane);
//                WaterShaderMod.vanillaShaders.setupVanillaShadersClippingPlanes(client, camera, plane);
            } else {
                Vector4f plane = new Vector4f(0.0f, -1.0f, 0.0f, 512f);
                WaterShaderMod.vanillaShaders.setupVanillaShadersClippingPlanes(client, client.player, plane);
//                WaterShaderMod.vanillaShaders.setupVanillaShadersClippingPlanes(client, camera, plane);
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/VertexBuffer;bind()V", shift = At.Shift.AFTER), method = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDDLorg/joml/Matrix4f;)V")
    public void inject(RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        int refractionColorTexture = WaterShaderMod.framebuffers.getRefractionTexture();
        int reflectionColorTexture = WaterShaderMod.framebuffers.getReflectionTexture();

        GL11.glDisable(GL11.GL_BLEND);

        int[] ints = new int[1];
        GL11.glGetIntegerv(GL20.GL_CURRENT_PROGRAM, ints);
        int program = ints[0];

        ShaderProgram currentProgram = client.gameRenderer.getProgram("rendertype_translucent");

        if (program == currentProgram.getGlRef()) {
            WaterShaderMod.vanillaShaders.setupWaterShader(client, reflectionColorTexture);
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();
        int minecraftFBO = client.getFramebuffer().fbo;
        int reflectionFBO = WaterShaderMod.framebuffers.getReflectionFBO();
        int refractionColorTexture = WaterShaderMod.framebuffers.getRefractionTexture();

        if (!WaterShaderMod.renderPass.doDrawWater()) {
            Framebuffers.CopyFrameBufferTexture(width, height, minecraftFBO, reflectionFBO);
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
}

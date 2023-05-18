package net.fabricmc.boduru.mixin;

import net.fabricmc.boduru.main.WaterShaderMod;
import net.fabricmc.boduru.shading.Framebuffers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.glDisable;

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
            /*MinecraftClient client = MinecraftClient.getInstance();
            Entity cameraclient = client.cameraEntity;

            Vec3d position = cameraclient.getPos();
            float pitch = cameraclient.getPitch();
//            cameraclient.setPos(position.x, position.y - 10, position.z);

//            Vec3d position = camera.getPos();
//            float pitch = camera.getPitch();
            WaterShaderMod.cameraSav.pitch = pitch;
            WaterShaderMod.cameraSav.position = position;

            float waterHeight = WaterShaderMod.clipPlane.getHeight();
            double d = 2 * (position.getY() - waterHeight);

            cameraclient.setPos(position.x, position.y - d, position.z);
            cameraclient.setPitch(-pitch);

            Vector4f plane = new Vector4f(0.0f, 1.0f, 0.0f, -waterHeight);
            WaterShaderMod.vanillaShaders.setupVanillaShadersClippingPlanes(client, cameraclient, plane);

//            ((CameraMixin) camera).invokeSetPos(position.x, position.y - d, position.z);
//            ((CameraMixin) camera).invokeSetPos(position.x, position.y - d, position.z);
//            ((CameraMixin) camera).setPitch(-pitch);*/
            } else {
                Vector4f plane = new Vector4f(0.0f, -1.0f, 0.0f, 512f);
                WaterShaderMod.vanillaShaders.setupVanillaShadersClippingPlanes(client, client.player, plane);
//                WaterShaderMod.vanillaShaders.setupVanillaShadersClippingPlanes(client, camera, plane);
            }
        }
    }

    //@Inject(at = @At("TAIL"), method = "render")
    private void renderTail(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        if (!WaterShaderMod.renderPass.doDrawWater()) {
            MinecraftClient client = MinecraftClient.getInstance();
            Entity cameraclient = client.cameraEntity;

            float pitch = WaterShaderMod.cameraSav.pitch;
            Vec3d position = WaterShaderMod.cameraSav.position;

            cameraclient.setPos(position.x, position.y, position.z);
            cameraclient.setPitch(pitch);

            float waterHeight = WaterShaderMod.clipPlane.getHeight();
            Vector4f plane = new Vector4f(0.0f, -1.0f, 0.0f, 512f);
            WaterShaderMod.vanillaShaders.setupVanillaShadersClippingPlanes(client, cameraclient, plane);

//            Vec3d position = cameraclient.getPos();

//            Vec3d position = camera.getPos();

//            float waterHeight = WaterShaderMod.clipPlane.getHeight();
//            double d = 2 * (position.getY() - waterHeight);

//            float pitch = WaterShaderMod.cameraSav.pitch;
//            Vec3d position = WaterShaderMod.cameraSav.position;
//
//            ((CameraMixin) camera).invokeSetPos(position.x, position.y, position.z);
//            ((CameraMixin) camera).setPitch(pitch);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/VertexBuffer;bind()V", shift = At.Shift.AFTER), method = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDDLorg/joml/Matrix4f;)V")
    public void inject(RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        int refractionColorTexture = WaterShaderMod.framebuffers.getRefractionColorBuffer();
        int reflectionColorTexture = WaterShaderMod.framebuffers.getReflectionTexture();

        glDisable(GL_BLEND);

        int[] ints = new int[1];
        GL11.glGetIntegerv(GL20.GL_CURRENT_PROGRAM, ints);
        int program = ints[0];

        ShaderProgram currentProgram = client.gameRenderer.getProgram("rendertype_translucent");

        if (program == currentProgram.getGlRef()) {
//            WaterShaderMod.vanillaShaders.setupWaterShaderTexture(client, refractionColorTexture);
//            if (Framebuffers.frameCount == 1) {
//                System.out.println("Shader texture number: OKOKOK");
////                currentProgram.addSampler("RefractionSampler", refractionColorTexture);
//            }
//            System.out.println("Shader texture number: " + ((ShaderProgramMixin) currentProgram).getSamplers().size());
//            System.out.println("Shader texture number: " + ((ShaderProgramMixin) currentProgram).getSamplerNames().size());
//            System.out.println("Shader texture number: " + ((ShaderProgramMixin) currentProgram).getLoadedSamplerIds().size());
//            for (String samplerName : ((ShaderProgramMixin) currentProgram).getSamplerNames()) {
//                System.out.println("Sampler name: " + samplerName);
//            }
            WaterShaderMod.vanillaShaders.setupWaterShaderTexture(client, reflectionColorTexture);
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();
        int worldFBO = WaterShaderMod.framebuffers.getWorldFrameBuffer();
        int minecraftFBO = client.getFramebuffer().fbo;
        int reflectionFBO = WaterShaderMod.framebuffers.getReflectionFBO();

        int worldColorTexture = WaterShaderMod.framebuffers.getWorldColorBuffer();
        int reflectionColorTexture = WaterShaderMod.framebuffers.getReflectionColorBuffer();
        int refractionColorTexture = WaterShaderMod.framebuffers.getRefractionColorBuffer();

        if (!WaterShaderMod.renderPass.doDrawWater()) {
            Framebuffers.CopyFrameBufferTexture(width, height, minecraftFBO, reflectionFBO);

            // Fill the reflection framebuffer with lightblue color
//            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, reflectionFBO);
//            GL11.glClearColor(0.5f, 0.5f, 1.0f, 1.0f);
//            GL11.glClearColor(0.0f, 0.3f, 0.8f, 1.0f);
//            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
//            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, minecraftFBO);
//            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
//            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        } else {
//            Framebuffers.CopyFrameBufferTexture(width, height, minecraftFBO, worldFBO);
//            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, minecraftFBO);
//            GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
//            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
//            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
//            GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
//            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
//            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, minecraftFBO);
//            WaterShaderMod.screenQuad.render(worldColorTexture, reflectionColorTexture, refractionColorTexture);




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

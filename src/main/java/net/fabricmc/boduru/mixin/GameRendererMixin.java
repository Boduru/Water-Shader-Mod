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
import org.jetbrains.annotations.Nullable;
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
//        if (!WaterShaderMod.renderPass.doDrawWater()) {
//            MinecraftClient client = MinecraftClient.getInstance();
//            Entity cameraclient = client.player;
//
//            if (cameraclient != null) {
//                Vec3d position = cameraclient.getPos();
//                float pitch = cameraclient.getPitch();
//
//                WaterShaderMod.cameraSav.pitch = pitch;
//                WaterShaderMod.cameraSav.position = position;
//
//                double d = 2 * (position.getY() - WaterShaderMod.clipPlane.getHeight());
//                cameraclient.setPos(position.getX(), d, position.getZ());
//                cameraclient.setPitch(-pitch);
//            }
//        }
//        else {
//            MinecraftClient client = MinecraftClient.getInstance();
//            Entity cameraclient = client.player;
//
//            Vec3d position = WaterShaderMod.cameraSav.position;
//
//            if (cameraclient != null) {
//                cameraclient.setPos(position.getX(), position.getY(), position.getZ());
//                cameraclient.setPitch(WaterShaderMod.cameraSav.pitch);
//            }
//        }


        if (!WaterShaderMod.renderPass.doDrawWater()) {
            MinecraftClient client = MinecraftClient.getInstance();
            Entity cameraclient = client.getCameraEntity();

            if (cameraclient != null) {
                Vec3d position = cameraclient.getPos();
                float pitch = cameraclient.getPitch();

                WaterShaderMod.cameraSav.pitch = pitch;
                WaterShaderMod.cameraSav.position = position;

                double d = 2 * (position.getY() - WaterShaderMod.clipPlane.getHeight());
                cameraclient.setPos(position.getX(), d, position.getZ());
                cameraclient.setPitch(-pitch);
            }
        }
        else {
            MinecraftClient client = MinecraftClient.getInstance();
            Entity cameraclient = client.getCameraEntity();

            Vec3d position = WaterShaderMod.cameraSav.position;

            if (cameraclient != null) {
                cameraclient.setPos(position.getX(), position.getY(), position.getZ());
                cameraclient.setPitch(WaterShaderMod.cameraSav.pitch);
            }
        }



        /*GameRenderer gameRenderer = (GameRenderer) (Object) this;
        Camera camera = gameRenderer.getCamera();

        if (!WaterShaderMod.renderPass.doDrawWater()) {
            if (camera != null) {
                Vec3d position = camera.getPos();
                float pitch = camera.getPitch();

                WaterShaderMod.cameraSav.pitch = pitch;
                WaterShaderMod.cameraSav.position = position;

                double d = 2 * (position.getY() - WaterShaderMod.clipPlane.getHeight());
                ((CameraMixin) camera).invokeSetPos(position.getX(), d, position.getZ());
                ((CameraMixin) camera).setPitch(-pitch);
            }
        } else {
            Vec3d position = WaterShaderMod.cameraSav.position;
            float pitch = WaterShaderMod.cameraSav.pitch;

            ((CameraMixin) camera).invokeSetPos(position.x, position.y, position.z);
            ((CameraMixin) camera).setPitch(pitch);
        }*/
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void renderTail(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        if (!WaterShaderMod.renderPass.doDrawWater()) {
            // Clipping plane pass
            GameRenderer gameRenderer = (GameRenderer) (Object) this;
            WaterShaderMod.renderPass.setDrawWater(true);
            gameRenderer.render(tickDelta, startTime, tick);
            WaterShaderMod.renderPass.setDrawWater(false);
        }
    }

    //@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V"))
    private void renderWorld(GameRenderer instance, float tickDelta, long limitTime, MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
//        int width = client.getWindow().getFramebufferWidth();
//        int height = client.getWindow().getFramebufferHeight();
//
//        int reflectionFBO = WaterShaderMod.framebuffers.getReflectionFBO();
//        int refractionFBO = WaterShaderMod.framebuffers.getRefractionFBO();
//        int worldFBO = WaterShaderMod.framebuffers.getWorldFrameBuffer();
//        int minecraftFBO = client.getFramebuffer().fbo;
//        int reflectionColorTexture = WaterShaderMod.framebuffers.getReflectionTexture();
//        int refractionColorTexture = WaterShaderMod.framebuffers.getRefractionTexture();
//        int worldColorTexture = WaterShaderMod.framebuffers.getWorldColorBuffer();
//
//        float waterHeight = WaterShaderMod.clipPlane.getHeight();

        if (client.player != null) {
//            Entity cameraclient = client.player;

//            Vec3d position = cameraclient.getPos();
//            float pitch = cameraclient.getPitch();

//            double d = 2 * (position.getY() - waterHeight);
//            cameraclient.setPitch(-pitch);
//            cameraclient.setPos(position.x, position.y - d, position.z);

            // Set clipping plane to cull everything below the water
//            Vector4f plane = new Vector4f(0.0f, 1.0f, 0.0f, -waterHeight);
//            WaterShaderMod.vanillaShaders.setupVanillaShadersClippingPlanes(client, client.player, plane);

            // Render reflection texture
            WaterShaderMod.renderPass.setDrawWater(false);
            instance.renderWorld(tickDelta, limitTime, matrices);

            if (Framebuffers.frameCount % 100 == 0) {
//                Framebuffers.CopyFrameBufferTexture(width, height, reflectionFBO, 0);
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

//            cameraclient.setPos(position.x, position.y, position.z);
//            cameraclient.setPitch(pitch);
//
//            // Set clipping plane to cull nothing
//            plane = new Vector4f(0.0f, -1.0f, 0.0f, 512f);
//            WaterShaderMod.vanillaShaders.setupVanillaShadersClippingPlanes(client, client.player, plane);

            WaterShaderMod.renderPass.setDrawWater(true);
            instance.renderWorld(tickDelta, limitTime, new MatrixStack());

            Framebuffers.frameCount++;
        }
    }
}

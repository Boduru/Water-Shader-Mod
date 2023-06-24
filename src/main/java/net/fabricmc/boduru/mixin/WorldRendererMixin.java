package net.fabricmc.boduru.mixin;

import net.fabricmc.boduru.main.WaterShaderMod;
import net.fabricmc.boduru.shading.Framebuffers;
import net.fabricmc.boduru.shading.RenderPass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(at = @At("HEAD"), method = "render")
    private void setupClippingPlane(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        float waterHeight = WaterShaderMod.clipPlane.getY();
        Vector4f plane;

        if (client.player != null) {
            if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.REFLECTION) {
                plane = new Vector4f(0.0f, 1.0f, 0.0f, -waterHeight);
            } else if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.REFRACTION) {
                plane = new Vector4f(0.0f, -1.0f, 0.0f, 256);
            } else {
                plane = new Vector4f(0.0f, -1.0f, 0.0f, 256f);
            }

//            float pitch = client.gameRenderer.getCamera().getPitch();
//            float yaw = client.gameRenderer.getCamera().getYaw();
            float pitch = client.player.getPitch();
            float yaw = client.player.getYaw();

            double eyeY = camera.getPos().getY() - ((CameraMixin) camera).getCameraY();

            float sneakOffset = (float) (1.6198292 - ((CameraMixin) camera).getCameraY());

            Vector3f pos = new Vector3f((float) camera.getPos().getX(), (float) eyeY + sneakOffset, (float) camera.getPos().getZ());
            WaterShaderMod.vanillaShaders.setupVanillaShadersClippingPlanes(client, pitch, yaw, pos, plane);
//            WaterShaderMod.vanillaShaders.setupVanillaShadersClippingPlanes(client, gameRenderer.getCamera(), plane);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/VertexBuffer;bind()V", shift = At.Shift.AFTER), method = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDDLorg/joml/Matrix4f;)V")
    public void setupWaterShaderParams(RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix, CallbackInfo ci) {
        WorldRenderer worldRenderer = (WorldRenderer) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        int refractionColorTexture = WaterShaderMod.framebuffers.getRefractionTexture();
        int reflectionColorTexture = WaterShaderMod.framebuffers.getReflectionTexture();
        int dudvmapTexture = WaterShaderMod.textureLoader.getTexture("dudvmap");

        GL11.glDisable(GL11.GL_BLEND);

        int[] ints = new int[1];
        GL11.glGetIntegerv(GL20.GL_CURRENT_PROGRAM, ints);
        int program = ints[0];

        ShaderProgram currentProgram = client.gameRenderer.getProgram("rendertype_translucent");

        if (program == currentProgram.getGlRef()) {
//            Vec3d vec3d = worldRenderer.client.getSkyColor(client.gameRenderer.getCamera().getPos(), tickDelta);
            WaterShaderMod.vanillaShaders.setupWaterShader(client, reflectionColorTexture, refractionColorTexture, dudvmapTexture);
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void renderToCustomFBO(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();
        int minecraftFBO = client.getFramebuffer().fbo;
        int reflectionFBO = WaterShaderMod.framebuffers.getReflectionFBO();
        int refractionFBO = WaterShaderMod.framebuffers.getRefractionFBO();

        if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.REFLECTION) {
            Framebuffers.CopyFrameBufferTexture(width, height, minecraftFBO, reflectionFBO);
        } else if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.REFRACTION) {
            Framebuffers.CopyFrameBufferTexture(width, height, minecraftFBO, refractionFBO);
        }
    }

    @Shadow
    private void renderLayer(RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix) {
    }

    @Redirect(method = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDDLorg/joml/Matrix4f;)V", ordinal = 5))
    private void redirectWaterDrawCall(WorldRenderer instance, RenderLayer renderLayer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix) {
        if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.WATER) {
            renderLayer(renderLayer, matrices, cameraX, cameraY, cameraZ, positionMatrix);
        }
    }

    @Redirect(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getSkyColor(Lnet/minecraft/util/math/Vec3d;F)Lnet/minecraft/util/math/Vec3d;", ordinal = 0))
    private Vec3d redirectGetSkyColor(ClientWorld instance, Vec3d cameraPos, float tickDelta) {
        Vec3d vec3d = instance.getSkyColor(cameraPos, tickDelta);

        if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.WATER) {
            WaterShaderMod.cameraSav.skyColor = vec3d.toVector3f();
        }

        return vec3d;
    }

    @Redirect(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/VertexBuffer;draw(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/gl/ShaderProgram;)V",
                    ordinal = 1))
    public void redirectDrawStars(VertexBuffer instance, Matrix4f viewMatrix, Matrix4f projectionMatrix, ShaderProgram program) {
        if (WaterShaderMod.renderPass.getCurrentPass() != RenderPass.Pass.REFLECTION) {
            instance.draw(viewMatrix, projectionMatrix, program);
        }
    }

    @Redirect(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FDDD)V"))
    public void redirectDrawClouds(WorldRenderer instance, MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, double d, double e, double f) {
        if (WaterShaderMod.renderPass.getCurrentPass() != RenderPass.Pass.REFLECTION) {
            instance.renderClouds(matrices, projectionMatrix, tickDelta, d, e, f);
        }
    }
}

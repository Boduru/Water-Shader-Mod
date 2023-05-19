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
    @Shadow @Final private Camera camera;

    @Inject(at = @At("HEAD"), method = "render")
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
//            Entity cameraclient = client.player;

            if (camera != null) {
//                Vec3d position = cameraclient.getCameraPosVec(tickDelta);
                Vec3d position = camera.getPos();
                float pitch = camera.getPitch();

                WaterShaderMod.cameraSav.pitch = pitch;
                WaterShaderMod.cameraSav.position = position;
                WaterShaderMod.cameraSav.cameraY = ((CameraMixin)camera).getCameraY();
                WaterShaderMod.cameraSav.lastCameraY = ((CameraMixin)camera).getLastCameraY();

                double d = 2 * (position.getY() - WaterShaderMod.clipPlane.getHeight());
                ((CameraMixin)camera).setPitch(-pitch);
                ((CameraMixin)camera).invokeSetPos(position.getX(), position.getY() - d, position.getZ());
                ((CameraMixin)camera).setCameraY((float) (position.getY() - d));
                ((CameraMixin)camera).setLastCameraY((float) (position.getY() - d));
                ((CameraMixin)camera).invokeSetRotation(-pitch, ((CameraMixin)camera).getYaw());

//                cameraclient.updatePositionAndAngles(position.getX(), position.getY() - d, position.getZ(), cameraclient.getYaw(), -pitch);
//                cameraclient.setPos(position.getX(), position.getY() - d, position.getZ());
//                cameraclient.setPitch(-pitch);
//                cameraclient.refreshPositionAndAngles(position.getX(), position.getY() - d, position.getZ(), -pitch, cameraclient.getYaw());
            }
        }
        else {
            MinecraftClient client = MinecraftClient.getInstance();
//            Entity cameraclient = client.player;

            Vec3d position = WaterShaderMod.cameraSav.position;
            float pitch = WaterShaderMod.cameraSav.pitch;
            float cameraY = WaterShaderMod.cameraSav.cameraY;
            float lastCameraY = WaterShaderMod.cameraSav.lastCameraY;

            if (camera != null) {
                ((CameraMixin)camera).setLastCameraY(lastCameraY);
                ((CameraMixin)camera).setCameraY(cameraY);
                ((CameraMixin)camera).setPitch(pitch);
                ((CameraMixin)camera).invokeSetPos(position.getX(), position.getY(), position.getZ());
                ((CameraMixin)camera).invokeSetRotation(pitch, ((CameraMixin)camera).getYaw());
//                cameraclient.setPos(position.getX(), position.getY(), position.getZ());
//                cameraclient.setPitch(WaterShaderMod.cameraSav.pitch);
//                cameraclient.updatePositionAndAngles(position.getX(), position.getY(), position.getZ(), cameraclient.getYaw(), WaterShaderMod.cameraSav.pitch);
//                cameraclient.refreshPositionAndAngles(position.getX(), position.getY(), position.getZ(), WaterShaderMod.cameraSav.pitch, cameraclient.getYaw());
            }
        }



//        GameRenderer gameRenderer = (GameRenderer) (Object) this;
//        Camera camera = gameRenderer.getCamera();
//
//        if (!WaterShaderMod.renderPass.doDrawWater()) {
//            if (camera != null) {
//                Vec3d position = camera.getPos();
//                float pitch = camera.getPitch();
//
//                WaterShaderMod.cameraSav.pitch = pitch;
//                WaterShaderMod.cameraSav.position = position;
//
//                double d = 2 * (position.getY() - WaterShaderMod.clipPlane.getHeight());
//                ((CameraMixin) camera).invokeSetPos(position.getX(), position.getY() - d, position.getZ());
//                ((CameraMixin) camera).setPitch(-pitch);
//                ((CameraMixin) camera).invokeSetRotation(camera.getPitch(), camera.getYaw());
//            }
//        } else {
//            Vec3d position = WaterShaderMod.cameraSav.position;
//            float pitch = WaterShaderMod.cameraSav.pitch;
//
//            ((CameraMixin) camera).invokeSetPos(position.x, position.y, position.z);
//            ((CameraMixin) camera).setPitch(pitch);
//            ((CameraMixin) camera).invokeSetRotation(camera.getPitch(), camera.getYaw());
//        }
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
}

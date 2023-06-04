package net.fabricmc.boduru.mixin;

import net.fabricmc.boduru.main.WaterShaderMod;
import net.fabricmc.boduru.shading.RenderPass;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private Camera camera;
    @Shadow private boolean renderHand;

//    @Inject(at = @At("HEAD"), method = "render")
//    private void renderHead(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
//        if (!WaterShaderMod.renderPass.doDrawWater()) {
//            if (camera != null) {
//                Vec3d position = camera.getPos();
//                float pitch = camera.getPitch();
//
//                WaterShaderMod.cameraSav.pitch = pitch;
//                WaterShaderMod.cameraSav.position = position;
//                WaterShaderMod.cameraSav.cameraY = ((CameraMixin) camera).getCameraY();
//                WaterShaderMod.cameraSav.lastCameraY = ((CameraMixin) camera).getLastCameraY();
//
//                double d = 2 * (position.getY() - WaterShaderMod.clipPlane.getHeight());
//                ((CameraMixin) camera).setPitch(-pitch);
//                ((CameraMixin)camera).invokeSetPos(position.getX(), position.getY() - d, position.getZ());
//                ((CameraMixin) camera).invokeSetRotation(-pitch, ((CameraMixin) camera).getYaw());
//            }
//        }
//        else {
//            Vec3d position = WaterShaderMod.cameraSav.position;
//            float pitch = WaterShaderMod.cameraSav.pitch;
//            float cameraY = WaterShaderMod.cameraSav.cameraY;
//            float lastCameraY = WaterShaderMod.cameraSav.lastCameraY;
//
//            if (camera != null) {
//                ((CameraMixin) camera).setPitch(pitch);
//                ((CameraMixin) camera).invokeSetRotation(pitch, ((CameraMixin) camera).getYaw());
//                ((CameraMixin)camera).invokeSetPos(position.getX(), position.getY(), position.getZ());
//            }
//        }
//    }

    @Shadow @Final private MinecraftClient client;

//        @Inject(method = "renderWorld", at = @At(
//            value = "INVOKE",
//            // Inject before the call to Camera.update()
//            target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V",
//            shift = At.Shift.BEFORE
//        ))
        private void transformationDown(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
            if (client.player == null) return;
            if (client.player.getPos() == null) return;

            double d = 2 * (client.player.getPos().getY() - WaterShaderMod.clipPlane.getHeight());
//            matrices.multiplyPositionMatrix(new Matrix4f().translate(0, (float) -d, 0));
//            matrices.translate(0, -d, 0);
        }

    @Inject(at = @At("HEAD"), method = "renderWorld")
    private void PostCameraUpdate(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.REFLECTION) {
            if (camera.getFocusedEntity() != null && client.player != null)  {
                // Saving
                WaterShaderMod.cameraSav.cameraPitch = camera.getPitch();
                WaterShaderMod.cameraSav.cameraPosition = camera.getPos();

                WaterShaderMod.cameraSav.playerPitch = client.player.getPitch();
                WaterShaderMod.cameraSav.playerPosition = client.player.getPos();

                float pitch = -camera.getPitch();
                double d = 2 * (client.player.getPos().getY() - WaterShaderMod.clipPlane.getHeight());

//                client.player.setPos(client.player.getPos().getX(), client.player.getPos().getY() - d, client.player.getPos().getZ());
                client.player.setPitch(pitch);
                //client.player.setPos(client.player.getPos().getX(), client.player.getPos().getY() - d, client.player.getPos().getZ());

                // Do not render hand
                renderHand = false;

                WaterShaderMod.vanillaShaders.setupVanillaShadersModelMatrices(client, 0, (float) d, 0);

                // Move camera as well
                Vec3d cameraPos = camera.getPos();
                ((CameraMixin) camera).setPitch(pitch);
                ((CameraMixin) camera).invokeSetRotation(pitch, ((CameraMixin) camera).getYaw());
//                ((CameraMixin) camera).invokeSetPos(cameraPos.getX(), cameraPos.getY(), cameraPos.getZ());
            }
        } else {
            renderHand = true;
            WaterShaderMod.vanillaShaders.setupVanillaShadersModelMatrices(client, 0, 0, 0);
        }

//        if (!WaterShaderMod.renderPass.doDrawWater()) {
//            if (camera != null) {
//                Vec3d position = camera.getPos();
//                float pitch = camera.getPitch();
//
//                WaterShaderMod.cameraSav.pitch = pitch;
//                WaterShaderMod.cameraSav.position = position;
//                WaterShaderMod.cameraSav.cameraY = ((CameraMixin) camera).getCameraY();
//                WaterShaderMod.cameraSav.lastCameraY = ((CameraMixin) camera).getLastCameraY();
//
//                double d = 2 * (position.getY() - WaterShaderMod.clipPlane.getHeight());
//                ((CameraMixin) camera).setPitch(-pitch);
//                ((CameraMixin)camera).invokeSetPos(position.getX(), position.getY() - d, position.getZ());
//                ((CameraMixin) camera).invokeSetRotation(-pitch, ((CameraMixin) camera).getYaw());
//            }
//        }
//        else {
//            Vec3d position = WaterShaderMod.cameraSav.position;
//            float pitch = WaterShaderMod.cameraSav.pitch;
//            float cameraY = WaterShaderMod.cameraSav.cameraY;
//            float lastCameraY = WaterShaderMod.cameraSav.lastCameraY;
//
//            if (camera != null) {
//                ((CameraMixin) camera).setPitch(pitch);
//                ((CameraMixin) camera).invokeSetRotation(pitch, ((CameraMixin) camera).getYaw());
//                ((CameraMixin)camera).invokeSetPos(position.getX(), position.getY(), position.getZ());
//            }
//        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void renderTail(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.REFLECTION) {
            // Clipping plane pass
            GameRenderer gameRenderer = (GameRenderer) (Object) this;
            WaterShaderMod.renderPass.nextRenderPass();

            // Restore camera
            MinecraftClient client = MinecraftClient.getInstance();

            if (camera.getFocusedEntity() != null && client.player != null && WaterShaderMod.cameraSav.playerPosition != null) {
                float pitch = WaterShaderMod.cameraSav.playerPitch;
                Vec3d position = WaterShaderMod.cameraSav.playerPosition;
//                client.player.setPos(position.getX(), position.getY(), position.getZ());
                client.player.setPitch(pitch);
                //client.player.setPos(position.getX(), position.getY(), position.getZ());

                // Move camera as well
                Vec3d cameraPosition = WaterShaderMod.cameraSav.cameraPosition;
                float cameraPitch = WaterShaderMod.cameraSav.cameraPitch;

                ((CameraMixin) camera).setPitch(cameraPitch);
                ((CameraMixin) camera).invokeSetRotation(cameraPitch, ((CameraMixin) camera).getYaw());
//                ((CameraMixin) camera).invokeSetPos(cameraPosition.getX(), cameraPosition.getY(), cameraPosition.getZ());
            }

            gameRenderer.render(tickDelta, startTime, tick);
        }

        if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.REFRACTION) {
            GameRenderer gameRenderer = (GameRenderer) (Object) this;
            WaterShaderMod.renderPass.nextRenderPass();

            gameRenderer.render(tickDelta, startTime, tick);
            WaterShaderMod.renderPass.nextRenderPass();

            WaterShaderMod.vanillaShaders.updateTimer(tickDelta);
        }
    }
}

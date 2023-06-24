package net.fabricmc.boduru.mixin;

import net.fabricmc.boduru.main.WaterShaderMod;
import net.fabricmc.boduru.shading.RenderPass;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private Camera camera;
    @Shadow private boolean renderHand;
    @Shadow @Final private MinecraftClient client;

    @Inject(at = @At("HEAD"), method = "renderWorld")
    private void PostCameraUpdate(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.REFLECTION) {
            if (camera.getFocusedEntity() != null && client.player != null)  {
                // Saving
                if (!client.player.isSneaking()) {
                    WaterShaderMod.cameraSav.cameraPitch = camera.getPitch();
                    WaterShaderMod.cameraSav.cameraPosition = camera.getPos();

                    WaterShaderMod.cameraSav.playerPitch = client.player.getPitch();
                    WaterShaderMod.cameraSav.playerPosition = client.player.getPos();
                }

                float pitch = -client.player.getPitch();
                client.player.setPitch(pitch);

                // Do not render hand
                renderHand = false;

                if (camera.isThirdPerson()) {
                    client.player.setInvisible(true);
                }

                double eyeY = (float) (camera.getPos().getY() - ((CameraMixin)camera).getCameraY()); // camera.getPos().getY() - ((CameraMixin)camera).getCameraY();

//                if (client.player.isSneaking()) {
//                    WaterShaderMod.cameraSav.cameraEyeYSneak = ((CameraMixin)camera).getCameraY();
////                    eyeY += 0.5;// WaterShaderMod.cameraSav.cameraPosition.getY();// - ((CameraMixin)camera).getCameraY();
//                } else {
//                    WaterShaderMod.cameraSav.cameraEyeYNoSneak = ((CameraMixin)camera).getCameraY();
//                }

                float sneakDiff = (float) (1.6198292 - ((CameraMixin) camera).getCameraY());

//                System.out.println(((CameraMixin) camera).getCameraY()); //1.6198292

                double d = 2 * (eyeY - WaterShaderMod.clipPlane.getY());
                WaterShaderMod.vanillaShaders.setupVanillaShadersModelMatrices(client, 0, (float) d, 0);
            }
        }
        else {
            renderHand = true;

            if (client.player != null) {
                client.player.setInvisible(false);
            }

            WaterShaderMod.vanillaShaders.setupVanillaShadersModelMatrices(client, 0, 0, 0);
        }
    }

    @Inject(method = "Lnet/minecraft/client/render/GameRenderer;bobView(Lnet/minecraft/client/util/math/MatrixStack;F)V", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void getBobViewParams(MatrixStack matrices, float tickDelta, CallbackInfo ci, PlayerEntity playerEntity, float f, float g, float h) {
        double tiltZ = Math.toDegrees(MathHelper.sin(g * 3.1415927F) * h * 3.0F);
        double tiltX = Math.toDegrees(Math.abs(MathHelper.cos(g * 3.1415927F - 0.2F) * h) * 5.0F);
        double translateX = MathHelper.sin(g * 3.1415927F) * h * 0.5F;
        double translateY = -Math.abs(MathHelper.cos(g * 3.1415927F) * h);

        WaterShaderMod.cameraSav.tiltX = (float) tiltX;
        WaterShaderMod.cameraSav.tiltZ = (float) tiltZ;

        WaterShaderMod.cameraSav.translateX = (float) translateX;
        WaterShaderMod.cameraSav.translateY = (float) translateY;
    }
//
//    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setInverseViewRotationMatrix(Lorg/joml/Matrix3f;)V"))
//    private void i(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
//        //WaterShaderMod.cameraSav.inverseViewMatrix = matrices.peek().getNormalMatrix().invert();
////        WaterShaderMod.cameraSav.viewMatrix = matrices.peek().getPositionMatrix();
////        matrixStack.multiplyPositionMatrix(this.getBasicProjectionMatrix(d));
//    }
//
//    @Shadow private void bobView(MatrixStack matrices, float tickDelta) {
//    }
    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;bobView(Lnet/minecraft/client/util/math/MatrixStack;F)V"))
    private void renderBobView(GameRenderer instance, MatrixStack matrices, float tickDelta) {
        if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.WATER) {
            //bobView(matrices, tickDelta);
        }
    }

//    @Inject(method = "bobView", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionf;)V", shift = At.Shift.AFTER, ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
//    private void bobViewCalculateViewMatrix(MatrixStack matrices, float tickDelta, CallbackInfo ci, PlayerEntity playerEntity, float f, float g, float h) {
//        WaterShaderMod.cameraSav.matrixStack.translate(MathHelper.sin(g * 3.1415927F) * h * 0.5F, -Math.abs(MathHelper.cos(g * 3.1415927F) * h), 0.0F);
//        WaterShaderMod.cameraSav.matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(g * 3.1415927F) * h * 3.0F));
//        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(Math.abs(MathHelper.cos(g * 3.1415927F - 0.2F) * h) * 5.0F));
//    }
//
    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V", ordinal = 0))
    private void camera(Camera instance, BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta) {
        instance.update(area, focusedEntity, thirdPerson, inverseView, tickDelta);

        WaterShaderMod.cameraSav.matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(instance.getPitch()));
        WaterShaderMod.cameraSav.matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(instance.getYaw() + 180.0F));

        WaterShaderMod.cameraSav.matrixStack.translate(instance.getPos().getX(), instance.getPos().getY(), instance.getPos().getZ());
    }

    @Inject(method = "renderWorld", at = @At(value = "TAIL"))
    private void tail(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        WaterShaderMod.cameraSav.matrixStack = new MatrixStack();
    }

//    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionf;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
//    private void renderWorldCalculateViewMatrix(float tickDelta, long limitTime, MatrixStack matrices1, CallbackInfo ci, MatrixStack matrices, boolean bl, Camera camera, MatrixStack matrixStack, double d, float f, float g) {
//
//    }

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
                client.player.setPitch(pitch);
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

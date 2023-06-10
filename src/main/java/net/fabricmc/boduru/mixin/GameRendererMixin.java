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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private Camera camera;
    @Shadow private boolean renderHand;
    @Shadow @Final private MinecraftClient client;

    @Shadow public abstract boolean isRenderingPanorama();

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

                float pitch = -client.player.getPitch();

                client.player.setPitch(pitch);

                // Do not render hand
                renderHand = false;

                double d = 2 * (client.player.getPos().getY() - WaterShaderMod.clipPlane.getHeight());
                WaterShaderMod.vanillaShaders.setupVanillaShadersModelMatrices(client, 0, (float) d, 0);
            }
        } else {
            renderHand = true;
            WaterShaderMod.vanillaShaders.setupVanillaShadersModelMatrices(client, 0, 0, 0);
        }
    }

//    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/render/GameRenderer;bobView(Lnet/minecraft/client/util/math/MatrixStack;F)V", cancellable = true)
//    private void UnTiltWaterEffect(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
//        if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.REFLECTION) {
//            ci.cancel();
//        }
//    }

//    @ModifyVariable(method = "Lnet/minecraft/client/render/GameRenderer;bobView(Lnet/minecraft/client/util/math/MatrixStack;F)V", at = @At("STORE"), ordinal = 2)
//    private float injected(float h) {
//        if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.REFLECTION || WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.REFRACTION) {
//            return 0;
//        }
//        return h;
//    }

//    @Shadow
//    private void bobView(MatrixStack matrices, float tickDelta) {
//
//    }
//
//    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;bobView(Lnet/minecraft/client/util/math/MatrixStack;F)V"), method = "renderWorld")
//    private void bob(GameRenderer gameRenderer, MatrixStack matrices, float tickDelta) {
//        if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.REFLECTION || WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.REFRACTION) {
//            // Do not bob
//        }
//        else {
//            bobView(matrices, tickDelta);
//        }
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

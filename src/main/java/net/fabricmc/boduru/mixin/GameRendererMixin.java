package net.fabricmc.boduru.mixin;

import net.fabricmc.boduru.main.WaterShaderMod;
import net.fabricmc.boduru.shading.RenderPass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
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
    @Shadow private boolean renderHand;

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setInverseViewRotationMatrix(Lorg/joml/Matrix3f;)V"), method = "renderWorld")
    private void PostCameraUpdate(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.REFLECTION) {
            if (camera.getFocusedEntity() != null && client.player != null)  {
                // Do not render hand
                renderHand = false;

                if (camera.isThirdPerson()) {
                    client.player.setInvisible(true);
                }

                double eyeY = (float) (camera.getPos().getY() - ((CameraMixin)camera).getCameraY());
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

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;bobView(Lnet/minecraft/client/util/math/MatrixStack;F)V"))
    private void renderBobView(GameRenderer instance, MatrixStack matrices, float tickDelta) {
        if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.WATER) {
            // bobView(matrices, tickDelta);
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void renderTail(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.REFLECTION) {
            // Clipping plane pass
            GameRenderer gameRenderer = (GameRenderer) (Object) this;
            WaterShaderMod.renderPass.nextRenderPass();

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

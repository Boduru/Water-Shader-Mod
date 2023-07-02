package net.fabricmc.boduru.mixin;

import net.fabricmc.boduru.main.WaterShaderMod;
import net.fabricmc.boduru.shading.RenderPass;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    /**
     * Method to invert the pitch of the player when rendering the reflection pass.
     */
    @Inject(at = @At("TAIL"), method = "getPitch", cancellable = true)
    private void getPitch(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.REFLECTION) {
            cir.setReturnValue(-cir.getReturnValue());
        }
    }
}

package net.fabricmc.boduru.mixin;

import net.fabricmc.boduru.main.WaterShaderMod;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(at = @At("HEAD"), method = "stop()V")
    private void stop(CallbackInfo ci) {
        WaterShaderMod.LOGGER.info("Water Shader Mod Stopped!");
        WaterShaderMod.FreeBuffers();
        WaterShaderMod.screenQuad.destroy();
    }
}

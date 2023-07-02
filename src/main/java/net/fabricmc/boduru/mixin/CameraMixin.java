package net.fabricmc.boduru.mixin;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Camera.class)
public interface CameraMixin {
    @Accessor("cameraY")
    float getCameraY();
}

package net.fabricmc.boduru.mixin;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraMixin {
    @Accessor("pitch")
    void setPitch(float pitch);

    @Invoker
    void invokeSetRotation(float pitch, float yaw);

    @Invoker
    void invokeSetPos(double x, double y, double z);

    @Invoker
    void invokeMoveBy(double x, double y, double z);
}

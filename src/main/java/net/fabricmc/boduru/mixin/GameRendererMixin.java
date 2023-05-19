package net.fabricmc.boduru.mixin;

import net.fabricmc.boduru.main.WaterShaderMod;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private Camera camera;

    @Inject(at = @At("HEAD"), method = "render")
    private void renderHead(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        if (!WaterShaderMod.renderPass.doDrawWater()) {
            if (camera != null) {
                Vec3d position = camera.getPos();
                float pitch = camera.getPitch();

                WaterShaderMod.cameraSav.pitch = pitch;
                WaterShaderMod.cameraSav.position = position;
                WaterShaderMod.cameraSav.cameraY = ((CameraMixin) camera).getCameraY();
                WaterShaderMod.cameraSav.lastCameraY = ((CameraMixin) camera).getLastCameraY();

                double d = 2 * (position.getY() - WaterShaderMod.clipPlane.getHeight());
                ((CameraMixin) camera).setPitch(-pitch);
//                ((CameraMixin)camera).invokeSetPos(position.getX(), position.getY() - d, position.getZ());
//                ((CameraMixin) camera).setCameraY((float) (position.getY() - d));
//                ((CameraMixin) camera).setLastCameraY((float) (position.getY() - d));
                ((CameraMixin) camera).invokeSetRotation(-pitch, ((CameraMixin) camera).getYaw());
            }
        }
        else {
            Vec3d position = WaterShaderMod.cameraSav.position;
            float pitch = WaterShaderMod.cameraSav.pitch;
            float cameraY = WaterShaderMod.cameraSav.cameraY;
            float lastCameraY = WaterShaderMod.cameraSav.lastCameraY;

            if (camera != null) {
//                ((CameraMixin) camera).setLastCameraY(lastCameraY);
//                ((CameraMixin) camera).setCameraY(cameraY);
                ((CameraMixin) camera).setPitch(pitch);
//                ((CameraMixin)camera).invokeSetPos(position.getX(), position.getY(), position.getZ());
                ((CameraMixin) camera).invokeSetRotation(pitch, ((CameraMixin) camera).getYaw());
            }
        }
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

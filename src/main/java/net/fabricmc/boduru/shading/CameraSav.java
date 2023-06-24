package net.fabricmc.boduru.shading;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CameraSav {
    public Vec3d playerPosition;
    public float playerPitch;
    public Vec3d cameraPosition;
    public float cameraPitch;
    public float tiltZ;
    public float tiltX;
    public float translateX;
    public float translateY;
    public Vector3f skyColor;
    public float cameraEyeYNoSneak;
    public float cameraEyeYSneak;
    public MatrixStack matrixStack = new MatrixStack();
}

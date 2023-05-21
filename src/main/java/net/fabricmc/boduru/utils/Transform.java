package net.fabricmc.boduru.utils;

import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Transform {
    /*public Vec3d position;
    public Vec3d eulerRot;

    public Transform(Vec3d position, Vec3d eulerRot) {
        this.position = position;
        this.eulerRot = eulerRot;
    }

    private interface RotateMatrixByAxisFunction
    {
        void invoke(MatrixStack matrix, float axisX, float axisY, float axisZ, float rotation);
    }

    private static RotateMatrixByAxisFunction rotateMatrixByAxis;

    static
    {
        rotateMatrixByAxis = GetRotateMatrixByAxis_V2();
    }

    public static void RotateMatrixByAxis(MatrixStack matrix, float axisX, float axisY, float axisZ, float rotation)
    {
        rotateMatrixByAxis.invoke(matrix, axisX, axisY, axisZ, rotation);
    }

    // >= 1.19.3
    private static RotateMatrixByAxisFunction GetRotateMatrixByAxis_V2()
    {
        // Vector3f
        Class<?> vectorType = ReflectionUtils.TryGetClass("org.joml.Vector3f");
        Constructor<?> vectorConstructor = ReflectionUtils.TryGetConstructor(vectorType, float.class, float.class, float.class);
        // RotationAxis
        Method rotationAxisOfMethod = ReflectionUtils.TryGetObfuscatedMethod(
                "net.minecraft.class_7833", "method_46356", "(Lorg/joml/Vector3f;)Lnet/minecraft/util/math/RotationAxis;",
                new Class<?>[] { vectorType }
        );
        Method rotationAxisRotationDegreesMethod = ReflectionUtils.TryGetObfuscatedMethod(
                "net.minecraft.class_7833", "rotationDegrees", "(F)Lorg/joml/Quaternionf;",
                new Class<?>[] { float.class }
        );
        // Quaternion
        Class<?> quaternionType = ReflectionUtils.TryGetClass("org.joml.Quaternionf");
        // MatrixStack
        Method matrixStackMultiplyMethod = ReflectionUtils.TryGetObfuscatedMethod(
                "net.minecraft.class_4587", "method_22907", "(Lorg/joml/Quaternionf;)V",
                new Class<?>[] { quaternionType }
        );

        if (vectorConstructor == null || rotationAxisOfMethod == null || rotationAxisRotationDegreesMethod == null || matrixStackMultiplyMethod == null) {
            return null;
        }

        return (matrix, axisX, axisY, axisZ, rotation) -> {
            Object vector = ReflectionUtils.TryCreateInstance(vectorConstructor, axisX, axisY, axisZ);
            Object rotationAxis = ReflectionUtils.TryInvokeMethod(rotationAxisOfMethod, null, vector);
            Object quaternion = ReflectionUtils.TryInvokeMethod(rotationAxisRotationDegreesMethod, rotationAxis, rotation);

            ReflectionUtils.TryInvokeMethod(matrixStackMultiplyMethod, matrix, quaternion);
        };
//    }
     */
}

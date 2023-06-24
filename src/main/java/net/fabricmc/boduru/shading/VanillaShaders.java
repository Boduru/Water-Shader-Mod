package net.fabricmc.boduru.shading;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.boduru.main.WaterShaderMod;
import net.fabricmc.boduru.mixin.CameraMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VanillaShaders {
    private List<String> terrainShaders;
    private String waterShader;
    private float waveStrength = 0.02f;
    private float timer = 0.0f;
    public static VanillaShaders Instance;

    private VanillaShaders() {
        terrainShaders = new ArrayList<>(Arrays.asList(
                "rendertype_cutout",
                "rendertype_cutout_mipped",
                "rendertype_crumbling",
                "rendertype_solid",
                "block"
        ));

        waterShader = "rendertype_translucent";
    }

    public static VanillaShaders getInstance() {
        if (Instance == null) {
            Instance = new VanillaShaders();
        }
        return Instance;
    }

    public void updateTimer(float tickDelta) {
        timer += tickDelta;
    }

    public void setMatrix4f(int shaderProgram, String uniformName, Matrix4f model) {
        // Use the shader program
        GL20.glUseProgram(shaderProgram);

        // Get the uniform location
        int uniformLocation = GL20.glGetUniformLocation(shaderProgram, uniformName);

        // Convert the matrix to a float buffer
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        model.get(buffer);

        // Set the new value
        GL20.glUniformMatrix4fv(uniformLocation, false, buffer);
    }

    public void setVector3f(int shaderProgram, String uniformName, Vector3f vector) {
        // Use the shader program
        GL20.glUseProgram(shaderProgram);

        // Get the uniform location
        int uniformLocation = GL20.glGetUniformLocation(shaderProgram, uniformName);

        // Set the new value
        GL20.glUniform3f(uniformLocation, vector.x, vector.y, vector.z);
    }

    public void setVector4f(int shaderProgram, String uniformName, Vector4f vector) {
        // Use the shader program
        GL20.glUseProgram(shaderProgram);

        // Get the uniform location
        int uniformLocation = GL20.glGetUniformLocation(shaderProgram, uniformName);

        // Set the new value
        GL20.glUniform4f(uniformLocation, vector.x, vector.y, vector.z, vector.w);
    }

    public void setupVanillaShadersClippingPlanes(MinecraftClient client, float pitch, float yaw, Vector3f pos, Vector4f plane) {
        // Calculate Inverse View Matrix
//        Matrix4f viewMatrix = createViewMatrix(camera.getPitch(), camera.getYaw(), camera.getEyePos().toVector3f());
        Matrix4f viewMatrix = createViewMatrix(pitch, yaw, pos);
        Matrix4f inverseViewMatrix = viewMatrix.invert();

        for (String shader : terrainShaders) {
            // Inverse View Matrix
            ShaderProgram sp = client.gameRenderer.getProgram(shader);
            setMatrix4f(sp.getGlRef(), "InverseViewMat", inverseViewMatrix);

            // Clip Plane
            setVector4f(sp.getGlRef(), "plane", plane);
        }
    }

    public void setupVanillaShadersModelMatrices(MinecraftClient client, float tx, float ty, float tz) {
        Matrix4f modelMatrix = createTranslationMatrix(tx, ty, tz);

        for (String shader : terrainShaders) {
            // Model Matrix
            ShaderProgram sp = client.gameRenderer.getProgram(shader);
            setMatrix4f(sp.getGlRef(), "CustomModelMatrix", modelMatrix);
        }
    }

    public static Matrix4f createViewMatrix(float pitch, float yaw, Vector3f position) {
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.identity();

//        pitch += Math.toRadians(WaterShaderMod.cameraSav.tiltX);
//        double roll = Math.toRadians(WaterShaderMod.cameraSav.tiltZ);

        viewMatrix.rotate((float) Math.toRadians(pitch), 1, 0, 0);
        viewMatrix.rotate((float) Math.toRadians(yaw), 0, 1, 0);
//        viewMatrix.rotate((float) Math.toRadians(roll), 0, 0, 1);

//        if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.REFLECTION) {
//            viewMatrix.rotate((float) Math.toRadians(WaterShaderMod.cameraSav.tiltX / 15), 1, 0, 0);
//            viewMatrix.rotate((float) Math.toRadians(WaterShaderMod.cameraSav.tiltZ / 15), 0, 0, 1);
//        }

//        position = position.add(new Vector3f(-WaterShaderMod.cameraSav.translateX, -WaterShaderMod.cameraSav.translateY, 0.0f));

        Vector3f negativeCameraPos = new Vector3f(-position.x, -position.y, -position.z);
        viewMatrix.translate(negativeCameraPos);

//        if (WaterShaderMod.renderPass.getCurrentPass() == RenderPass.Pass.REFLECTION) {
//            viewMatrix.translate(WaterShaderMod.cameraSav.translateX, WaterShaderMod.cameraSav.translateY, 0.0f);
//        }

        return viewMatrix;
    }

    public static Matrix4f createTranslationMatrix(float tx, float ty, float tz) {
        Matrix4f translationMatrix = new Matrix4f();
        translationMatrix.identity();

        translationMatrix.translate(tx, ty, tz);

        return translationMatrix;
    }

    public void setupWaterShader(MinecraftClient client, int reflectionTexture, int refractionTexture, int dudvmap) {
        // Use the shader program
        ShaderProgram sp = client.gameRenderer.getProgram(waterShader);

        // Get the uniform location
        int uniformLocationReflec = GL20.glGetUniformLocation(sp.getGlRef(), "reflectionTexture");
        int uniformLocationRefrac = GL20.glGetUniformLocation(sp.getGlRef(), "refractionTexture");
        int uniformLocationDUDV = GL20.glGetUniformLocation(sp.getGlRef(), "dudvmap");

        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, reflectionTexture);

        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, refractionTexture);

        GL13.glActiveTexture(GL13.GL_TEXTURE4);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, dudvmap);

        // Set the new value
        GL20.glUniform1i(uniformLocationReflec, 2);
        GL20.glUniform1i(uniformLocationRefrac, 3);
        GL20.glUniform1i(uniformLocationDUDV, 4);

        // Setup Screen Size
        int screenWidthLoc = GL20.glGetUniformLocation(sp.getGlRef(), "screenWidth");
        GL20.glUniform1i(screenWidthLoc, client.getWindow().getFramebufferWidth());

        int screenHeightLoc = GL20.glGetUniformLocation(sp.getGlRef(), "screenHeight");
        GL20.glUniform1i(screenHeightLoc, client.getWindow().getFramebufferHeight());

        // Set Inverse View Matrix
        if (client.player == null) return;
        Camera camera = client.gameRenderer.getCamera();
        float eyeY = (float) (camera.getPos().getY() - ((CameraMixin)camera).getCameraY()); // (float) (camera.getPos().getY() - ((CameraMixin)camera).getCameraY())
        Vector3f cameraPos = new Vector3f((float) camera.getPos().getX(), eyeY, (float) camera.getPos().getZ());

//        float pitch = client.player.getPitch();
//        System.out.println(pitch);

//        float pitch = client.player.getPitch(); // camera.getPitch();
//        float yaw = client.player.getYaw(); // camera.getYaw();

        float pitch = camera.getPitch();
        float yaw = camera.getYaw();

        Matrix4f viewMatrix = createViewMatrix(pitch, yaw, cameraPos);
        Matrix4f inverseViewMatrix = viewMatrix.invert();

//        inverseViewMatrix = WaterShaderMod.cameraSav.matrixStack.peek().getPositionMatrix().invert();

//        inverseViewMatrix = RenderSystem.getModelViewMatrix().invert();

        setMatrix4f(sp.getGlRef(), "InverseViewMat", inverseViewMatrix);

        // Set Custom Sneaking Offset Matrix
        //float diffY = WaterShaderMod.cameraSav.cameraEyeYNoSneak - WaterShaderMod.cameraSav.cameraEyeYSneak;
        float sneakOffset = (float) (1.6198292 - ((CameraMixin) camera).getCameraY());
        Matrix4f customSneakingOffsetMatrix = createTranslationMatrix(0.0f, sneakOffset, 0.0f);

        customSneakingOffsetMatrix = customSneakingOffsetMatrix.identity();

        setMatrix4f(sp.getGlRef(), "CustomSneakingOffsetMat", customSneakingOffsetMatrix);

//        if (client.player.isSneaking()) {
//            setMatrix4f(sp.getGlRef(), "CustomSneakingOffsetMat", customSneakingOffsetMatrix);
//        } else {
//            setMatrix4f(sp.getGlRef(), "CustomSneakingOffsetMat", new Matrix4f().identity());
//        }

        // Set Pitch
        if (client.player == null) return;
        int pitchLoc = GL20.glGetUniformLocation(sp.getGlRef(), "pitch");
        GL20.glUniform1f(pitchLoc, camera.getPitch());

        // Set Sky Color
        setVector3f(sp.getGlRef(), "skyColor", WaterShaderMod.cameraSav.skyColor);

        // Set Camera Position
        setVector3f(sp.getGlRef(), "cameraPos", cameraPos);

        // Set Timer
        int timerLoc = GL20.glGetUniformLocation(sp.getGlRef(), "timer");
        GL20.glUniform1f(timerLoc, timer);

        // Set Sneak Offset
        int sneakOffsetLoc = GL20.glGetUniformLocation(sp.getGlRef(), "sneakOffset");
        GL20.glUniform1f(sneakOffsetLoc, sneakOffset);
    }
}

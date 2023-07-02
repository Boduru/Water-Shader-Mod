package net.fabricmc.boduru.shading;

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
    private final List<String> terrainShaders;
    private final String waterShader;
    private float timer = 0.0f;
    private static VanillaShaders instance;

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
        if (instance == null) {
            instance = new VanillaShaders();
        }
        return instance;
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
        Matrix4f viewMatrix = createViewMatrix(pitch, yaw, pos);
        Matrix4f inverseViewMatrix = viewMatrix.invert();

        for (String shader : terrainShaders) {
            // Inverse View Matrix
            ShaderProgram sp = client.gameRenderer.getProgram(shader);

            if (sp != null) {
                setMatrix4f(sp.getGlRef(), "InverseViewMat", inverseViewMatrix);
                setVector4f(sp.getGlRef(), "plane", plane);
            }
        }
    }

    public void setupVanillaShadersModelMatrices(MinecraftClient client, float tx, float ty, float tz) {
        Matrix4f modelMatrix = createTranslationMatrix(tx, ty, tz);

        for (String shader : terrainShaders) {
            // Model Matrix
            ShaderProgram sp = client.gameRenderer.getProgram(shader);

            if (sp != null) {
                setMatrix4f(sp.getGlRef(), "CustomModelMatrix", modelMatrix);
            }
        }
    }

    public static Matrix4f createViewMatrix(float pitch, float yaw, Vector3f position) {
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.identity();

        viewMatrix.rotate((float) Math.toRadians(pitch), 1, 0, 0);
        viewMatrix.rotate((float) Math.toRadians(yaw), 0, 1, 0);

        Vector3f negativeCameraPos = new Vector3f(-position.x, -position.y, -position.z);
        viewMatrix.translate(negativeCameraPos);

        return viewMatrix;
    }

    public static Matrix4f createTranslationMatrix(float tx, float ty, float tz) {
        Matrix4f translationMatrix = new Matrix4f();
        translationMatrix.identity();

        translationMatrix.translate(tx, ty, tz);

        return translationMatrix;
    }

    public void setupWaterShader(MinecraftClient client, int reflectionTexture, int refractionTexture) {
        // Get water shader program
        ShaderProgram sp = client.gameRenderer.getProgram(waterShader);

        if (sp == null || client.player == null) return;

        // Get the uniform location
        int uniformLocationReflec = GL20.glGetUniformLocation(sp.getGlRef(), "reflectionTexture");
        int uniformLocationRefrac = GL20.glGetUniformLocation(sp.getGlRef(), "refractionTexture");

        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, reflectionTexture);

        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, refractionTexture);

        // Set the new value
        GL20.glUniform1i(uniformLocationReflec, 2);
        GL20.glUniform1i(uniformLocationRefrac, 3);

        // Setup Screen Size
        int screenWidthLoc = GL20.glGetUniformLocation(sp.getGlRef(), "screenWidth");
        GL20.glUniform1i(screenWidthLoc, client.getWindow().getFramebufferWidth());

        int screenHeightLoc = GL20.glGetUniformLocation(sp.getGlRef(), "screenHeight");
        GL20.glUniform1i(screenHeightLoc, client.getWindow().getFramebufferHeight());

        // Set Inverse View Matrix
        Camera camera = client.gameRenderer.getCamera();
        float eyeY = (float) (camera.getPos().getY() - ((CameraMixin)camera).getCameraY());

        Vector3f cameraPos = new Vector3f((float) camera.getPos().getX(), eyeY, (float) camera.getPos().getZ());

        float pitch = camera.getPitch();
        float yaw = camera.getYaw();

        Matrix4f viewMatrix = createViewMatrix(pitch, yaw, cameraPos);
        Matrix4f inverseViewMatrix = viewMatrix.invert();

        setMatrix4f(sp.getGlRef(), "InverseViewMat", inverseViewMatrix);

        // Set Custom Sneaking Offset
        float sneakOffset = (float) (1.6198292 - ((CameraMixin) camera).getCameraY());

        // Set Pitch
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

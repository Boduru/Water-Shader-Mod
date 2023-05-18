package net.fabricmc.boduru.shading;

import net.fabricmc.boduru.mixin.ShaderProgramMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
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
    private boolean isWaterShaderSetup = false;
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

        // Stop using the shader program
        GL20.glUseProgram(0);
    }

    public void setVector4f(int shaderProgram, String uniformName, Vector4f vector) {
        // Use the shader program
        GL20.glUseProgram(shaderProgram);

        // Get the uniform location
        int uniformLocation = GL20.glGetUniformLocation(shaderProgram, uniformName);

        // Set the new value
        GL20.glUniform4f(uniformLocation, vector.x, vector.y, vector.z, vector.w);

        // Stop using the shader program
        GL20.glUseProgram(0);
    }

//    public void setupVanillaShadersClippingPlanes(MinecraftClient client, Entity camera, Vector4f plane) {
//        // Calculate Inverse View Matrix
//        Matrix4f viewMatrix = createViewMatrix(camera.getPitch(), camera.getYaw(), camera.getPos().toVector3f());
//        Matrix4f inverseViewMatrix = viewMatrix.invert();
//
//        for (String shader : terrainShaders) {
//            // Inverse View Matrix
//            ShaderProgram sp = client.gameRenderer.getProgram(shader);
//            setMatrix4f(sp.getGlRef(), "InverseViewMat", inverseViewMatrix);
//
//            // Clip Plane
//            setVector4f(sp.getGlRef(), "plane", plane);
//        }
//    }

    public void setupVanillaShadersClippingPlanes(MinecraftClient client, Camera camera, Vector4f plane) {
        // Calculate Inverse View Matrix
        Matrix4f viewMatrix = createViewMatrix(camera.getPitch(), camera.getYaw(), camera.getPos().toVector3f());
        Matrix4f inverseViewMatrix = viewMatrix.invert();

        for (String shader : terrainShaders) {
            // Inverse View Matrix
            ShaderProgram sp = client.gameRenderer.getProgram(shader);
            setMatrix4f(sp.getGlRef(), "InverseViewMat", inverseViewMatrix);

            // Clip Plane
            setVector4f(sp.getGlRef(), "plane", plane);
        }
    }

    public void setupVanillaShadersClippingPlanes(MinecraftClient client, Entity camera, Vector4f plane) {
        // Calculate Inverse View Matrix
        Matrix4f viewMatrix = createViewMatrix(camera.getPitch(), camera.getYaw(), camera.getPos().toVector3f());
        Matrix4f inverseViewMatrix = viewMatrix.invert();

        for (String shader : terrainShaders) {
            // Inverse View Matrix
            ShaderProgram sp = client.gameRenderer.getProgram(shader);
            setMatrix4f(sp.getGlRef(), "InverseViewMat", inverseViewMatrix);

            // Clip Plane
            setVector4f(sp.getGlRef(), "plane", plane);
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

    public void setupWaterShaderTexture(MinecraftClient client, int texture) {
        // Use the shader program
        ShaderProgram sp = client.gameRenderer.getProgram(waterShader);

//        GL20.glUseProgram(sp.getGlRef());

        //3553
        // Get the uniform location
        int uniformLocation = GL20.glGetUniformLocation(sp.getGlRef(), "Sampler1");

        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

        // Set the new value
        GL20.glUniform1i(uniformLocation, 3);

        // Stop using the shader program
//        GL20.glUseProgram(0);
    }

    public void setupTranslucentShaderTexture(MinecraftClient client, int texture) {
        // Use the shader program
        ShaderProgram sp = client.gameRenderer.getProgram("rendertype_translucent");

        if (!isWaterShaderSetup) {
            int samplerID = GL20.glGetUniformLocation(sp.getGlRef(), "Sampler12");
            sp.addSampler("Sampler12", texture);
            ((ShaderProgramMixin)sp).getLoadedSamplerIds().add(samplerID);
            ((ShaderProgramMixin)sp).getSamplerNames().add("Sampler12");
            ((ShaderProgramMixin)sp).getSamplers().put("Sampler12", texture);

            isWaterShaderSetup = true;
        }
    }
}

package net.fabricmc.boduru.shading;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

public class ScreenQuad {
    private final String vertexShaderFile = "../src/main/resources/assets/water_shader/shaders/custom/water.vsh";
    private final String fragmentShaderFile = "../src/main/resources/assets/water_shader/shaders/custom/water.fsh";

    private float[] vertices = {
            // position, texture coordinates
            -1.0f, 1.0f, 0.0f,  0.0f, 1.0f,
            1.0f, 1.0f, 0.0f,   1.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 0.0f,  1.0f, 0.0f,
    };

    private int[] elements = {
            0, 2, 1,
            1, 2, 3
    };

    private Shader shader;
    private int vao;
    private int vbo;
    private int ebo;

    public ScreenQuad() {
        // Create VAO and VBO and upload the data
        setup();

        // Create the triangle shader
        shader = new Shader(vertexShaderFile, fragmentShaderFile)
                .useProgram()
                .setAttribute("position", 3, 5, 0)
                .setAttribute("texCoords", 2, 5, 3);

        // Stop using the VAO, VBO, and EBO
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void setup() {
        // Create triangle VAO
        vao = glGenVertexArrays();

        // Bind VAO (start using it/make it the active VAO)
        glBindVertexArray(vao);

        // Create the vertex position VBO
        vbo = glGenBuffers();

        // Bind the VBO (start using it/make it the active VBO)
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // Upload data to the GPU in the VBO
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        // Create the EBO
        ebo = glGenBuffers();

        // Bind the EBO
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);

        // Upload the data
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elements, GL_STATIC_DRAW);
    }

    public void render(int worldTexture, int reflectionTexture, int refractionTexture) {
        shader.useProgram();

        glDisable(GL_BLEND);

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);

//        glDisable(GL_CULL_FACE);

        int worldTexLoc = GL20.glGetUniformLocation(shader.getShaderProgram(), "worldTexture");
        int reflectionTexLoc = GL20.glGetUniformLocation(shader.getShaderProgram(), "reflectionTexture");

        GL20.glUniform1i(worldTexLoc, 0);
        GL20.glUniform1i(reflectionTexLoc, 1);

        // Minecraft viewport textures
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, worldTexture);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, reflectionTexture);

//        glActiveTexture(GL_TEXTURE2);
//        glBindTexture(GL_TEXTURE_2D, refractionTexture);
//        uniformLocation = GL20.glGetUniformLocation(shader.getShaderProgram(), "refractionTexture");
//        GL20.glUniform1i(uniformLocation, 2);

        // Draw the triangle
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        // Stop using shader
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        shader.stopProgram();

//        glEnable(GL_CULL_FACE);
    }

    public void destroy() {
        // Delete the shader
        shader.deleteShaderProgram();

        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteVertexArrays(vao);
    }
}

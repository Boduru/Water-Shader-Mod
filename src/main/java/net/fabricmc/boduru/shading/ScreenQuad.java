package net.fabricmc.boduru.shading;

import org.joml.Matrix4f;

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

    public void render(int id) {
        shader.useProgram();

        // Use VAO
        glBindVertexArray(vao);

        // Use VBO
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // Use EBO
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);

        // Use shader
        shader.useProgram();

        // Activate texture
        glActiveTexture(GL_TEXTURE0);

        // Bind the texture
        glBindTexture(GL_TEXTURE_2D, id);

        // Draw the triangle
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        // Stop using shader
        shader.stopProgram();

        // Stop using the EBO
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        // Stop using the VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        // Stop using the VAO
        glBindVertexArray(0);
    }

    public void destroy() {
        // Delete the shader
        shader.deleteShaderProgram();

        // Delete the VBO
        glDeleteBuffers(vbo);

        // Delete the EBO
        glDeleteBuffers(ebo);

        // Delete the VAO
        glDeleteVertexArrays(vao);
    }
}

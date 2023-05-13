package net.fabricmc.boduru.shading;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private int shaderProgram;
    private int vertexShader;
    private int fragmentShader;

    public Shader(String vshFile, String fshFile) {
        // Create shaders
        setup(vshFile, fshFile);
    }

    public Shader setAttribute(String attributeName, int size, int stride, int pointer) {
        // Retrieve location of the position attribute in the shader program
        int locationPosition = glGetAttribLocation(shaderProgram, attributeName);

        // Tell OpenGL how to access the data (attribute, number of elements, to normalize?, stride, start pointer)
        glVertexAttribPointer(locationPosition, size, GL_FLOAT, false, stride * Float.BYTES, (long) pointer * Float.BYTES);

        enableVertexAttribute(locationPosition);

        return this;
    }

    public void setup(String vshFile, String fshFile) {
        String vshCode = readFile(vshFile);
        String fshCode = readFile(fshFile);

        // Create vertex shader program
        vertexShader = glCreateShader(GL_VERTEX_SHADER);

        // Upload code to the vertex shader program
        glShaderSource(vertexShader, vshCode);

        // Compile vertex shader program
        glCompileShader(vertexShader);

        if (GL20.glGetShaderi(vertexShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.out.println(GL20.glGetShaderInfoLog(vertexShader, 500));
            System.err.println("Shader::Could not compile vertex shader!");
        }

        // Create fragment shader program
        fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);

        // Upload code to the fragment shader program
        glShaderSource(fragmentShader, fshCode);

        // Compile fragment shader program
        glCompileShader(fragmentShader);

        if (GL20.glGetShaderi(fragmentShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.out.println(GL20.glGetShaderInfoLog(fragmentShader, 500));
            System.err.println("Shader::Could not compile fragment shader!");
            System.exit(-1);
        }

        // Create shader program
        shaderProgram = glCreateProgram();

        // Attach vertex and fragment shaders to the shader program
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);

        // Link shader program
        glLinkProgram(shaderProgram);

        // Delete the vertex shader
        glDeleteShader(vertexShader);

        // Delete the fragment shader
        glDeleteShader(fragmentShader);
    }

    public Shader setUniform3f(String uniformName, float x, float y, float z) {
        // Get the uniform location
        int uniformLocation = glGetUniformLocation(shaderProgram, uniformName);

        // Set the new value
        glUniform3f(uniformLocation, x, y, z);

        return this;
    }

    public Shader setUniform4f(String uniformName, float x, float y, float z, float w) {
        // Get the uniform location
        int uniformLocation = glGetUniformLocation(shaderProgram, uniformName);

        // Set the new value
        glUniform4f(uniformLocation, x, y, z, w);

        return this;
    }

    public Shader setMatrix4f(String uniformName, Matrix4f model) {
        // Get the uniform location
        int uniformLocation = glGetUniformLocation(shaderProgram, uniformName);

        // Convert the matrix to a float buffer
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        model.get(buffer);

        // Set the new value
        glUniformMatrix4fv(uniformLocation, false, buffer);

        return this;
    }

    public Shader setUniform1f(String uniformName, float v) {
        // Get the uniform location
        int uniformLocation = glGetUniformLocation(shaderProgram, uniformName);

        // Set the new value
        glUniform1f(uniformLocation, v);

        return this;
    }

    public Shader enableVertexAttribute(int i) {
        // Enable the attribute
        glEnableVertexAttribArray(i);

        return this;
    }

    public Shader useProgram() {
        // Use the shader program (make the current shader program active)
        glUseProgram(shaderProgram);

        return this;
    }

    public void stopProgram() {
        // Stop using the program
        glUseProgram(0);
    }

    public int getShaderProgram() {
        return shaderProgram;
    }

    public static String readFile(String filepath) {
        Path filePath = Path.of(filepath);
        String content = "";

        try {
            content = Files.readString(filePath);
        } catch (IOException e) {
            System.err.println("Shader::Could not read file: " + filePath);
            throw new RuntimeException(e);
        }

        return content;
    }

    public void deleteShaderProgram() {
        // Stop using the shader program
        glUseProgram(0);

        // Delete the shader program
        glDeleteProgram(shaderProgram);
    }
}

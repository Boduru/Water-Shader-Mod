#version 150

in vec3 position;
in vec2 texCoords;

out vec2 TexCoords;

//uniform mat4 model;

void main() {
    // Output the position in normalized device coordinates -> [-1, 1]
    gl_Position = vec4(position, 1.0);

    TexCoords = texCoords;
}
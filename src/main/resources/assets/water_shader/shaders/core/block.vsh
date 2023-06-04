#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec2 UV2;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec2 texCoord0;
out vec2 texCoord2;
out vec4 normal;

uniform mat4 InverseViewMat;
uniform mat4 CustomModelMatrix;
uniform vec4 plane;

void main() {
    vec4 viewPosition = ModelViewMat * CustomModelMatrix * vec4(Position, 1.0);

    gl_ClipDistance[0] = dot(InverseViewMat * ModelViewMat * vec4(Position, 1.0), plane);

    gl_Position = ProjMat * viewPosition;

    vertexColor = Color;
    texCoord0 = UV0;
    texCoord2 = UV2;
    normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
}

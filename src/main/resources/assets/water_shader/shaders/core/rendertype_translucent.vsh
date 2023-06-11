#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 InverseViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;
uniform int FogShape;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec4 normal;
out vec4 worldPos;
out vec2 dudvMapUVCoords;
out vec4 clipSpace;

void main() {
    vec3 pos = Position + ChunkOffset;
    worldPos = InverseViewMat * ModelViewMat * vec4(pos, 1.0);
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    clipSpace = gl_Position;
    dudvMapUVCoords = vec2(Position.x / 2 + 0.5, Position.y / 2 + 0.5) * 6.0;

    vertexDistance = fog_distance(ModelViewMat, pos, FogShape);
    vertexColor = Color * minecraft_sample_lightmap(Sampler2, UV2);
    texCoord0 = UV0;
    normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
}

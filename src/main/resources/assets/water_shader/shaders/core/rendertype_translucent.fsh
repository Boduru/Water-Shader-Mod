#version 450 core

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
layout (binding = 3) uniform sampler2D Sampler1;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

uniform int screenWidth;
uniform int screenHeight;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;
in vec4 clipspace;

out vec4 fragColor;

void main() {
//    vec2 ndc = (clipspace.xy / clipspace.w) / 2.0 + 0.5;
//    vec2 reflectionCoords = vec2(ndc.x, -ndc.y);
    vec2 reflectionCoords = vec2(gl_FragCoord.x / screenWidth, -gl_FragCoord.y / screenHeight);

    vec4 reflectionColor = texture(Sampler1, reflectionCoords);
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    color = reflectionColor;

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}

#version 450 core

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler3;

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

uniform float pitch;

in vec3 toCamera;

out vec4 fragColor;

void main() {
    vec2 reflectionCoords = vec2(gl_FragCoord.x / screenWidth, -gl_FragCoord.y / screenHeight);
    vec2 refractionCoords = vec2(gl_FragCoord.x / screenWidth, gl_FragCoord.y / screenHeight);

    vec4 reflectionColor = texture(Sampler1, reflectionCoords);
    vec4 refractionColor = texture(Sampler3, refractionCoords);
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;

    vec3 viewVector = normalize(toCamera);
    float fresnel = pow(dot(viewVector, vec3(0, 1, 0)), 1);

//    float fresnel = clamp(pitch / 90.0 * 1.4f, 0.05f, 0.95f);

    color = mix(reflectionColor, refractionColor, fresnel);
    color = mix(color, vec4(0.0, 0.2, 0.5, 1.0), 0.2);

    fragColor = fragColor;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}

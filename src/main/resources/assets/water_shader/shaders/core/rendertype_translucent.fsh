#version 450 core

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform sampler2D reflectionTexture;
uniform sampler2D refractionTexture;
uniform sampler2D dudvmap;

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
in vec3 worldPos;

uniform float pitch;
uniform float timer;
uniform float waveStrength;

out vec4 fragColor;

void main() {
    if (worldPos.y > 62) {
        fragColor = vec4(0.0, 0.25, 0.6, 1.0);
        return;
    }

    // Calculate reflection and refraction texture coordinates (mirror-like effect)
    vec2 reflectionCoords = vec2(gl_FragCoord.x / screenWidth, -gl_FragCoord.y / screenHeight);
    vec2 refractionCoords = vec2(gl_FragCoord.x / screenWidth, gl_FragCoord.y / screenHeight);

    // Distortion
    vec2 distortion = texture(dudvmap, vec2(texCoord0.x + timer * 0.06, texCoord0.y)).rg;
    distortion = (distortion * 2.0 - 1.0) * waveStrength;

//    refractionCoords += distortion;
//    refractionCoords = clamp(refractionCoords, 0.001, 0.009);
//
//    reflectionCoords += distortion;
//    reflectionCoords.x = clamp(reflectionCoords.x, 0.001, 0.009);
//    refractionCoords.y = clamp(refractionCoords.y, -0.009, -0.001);

    // Sample reflection and refraction textures
    vec4 reflectionColor = texture(reflectionTexture, reflectionCoords);
    vec4 refractionColor = texture(refractionTexture, refractionCoords);
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;

    // Calculate fresnel (reflection/refraction mix depending on viewing angle)
    float fresnel = clamp(pitch / 90.0, 0.3f, 0.7f);

    color = mix(reflectionColor, refractionColor, fresnel);
    color = mix(color, vec4(0.0, 0.25, 0.6, 1.0), 0.30);

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}

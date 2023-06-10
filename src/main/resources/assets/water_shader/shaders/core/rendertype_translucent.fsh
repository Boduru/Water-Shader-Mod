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
in vec4 worldPos;
in vec2 dudvMapUVCoords;
in vec4 clipSpace;

uniform float pitch;
uniform float timer;
uniform float waveStrength;

out vec4 fragColor;

float getDepth() {
    float near = 0.1;
    float far  = 100.0;
    float z = gl_FragCoord.z * 2.0 - 1.0; // back to NDC
    float depth = (2.0 * near * far) / (far + near - z * (far - near));
    depth = depth / far;

    return depth;
}

void main() {
    // Calculate reflection and refraction texture coordinates (mirror-like effect)
    vec2 reflectionCoords = vec2(gl_FragCoord.x / screenWidth, -gl_FragCoord.y / screenHeight);
    vec2 refractionCoords = vec2(gl_FragCoord.x / screenWidth, gl_FragCoord.y / screenHeight);

    // Distortion
//    vec2 distortion = texture(dudvmap, vec2(dudvMapUVCoords.x, dudvMapUVCoords.y)).rg;
//    distortion = (distortion * 2.0 - 1.0) * waveStrength + timer * 0.01;
//
//    refractionCoords += distortion;
//    refractionCoords = clamp(refractionCoords, 0.005, 0.995);
//
//    reflectionCoords += distortion;
//    reflectionCoords.x = clamp(reflectionCoords.x, 0.005, 0.995);
//    refractionCoords.y = clamp(refractionCoords.y, -0.995, -0.005);

    vec2 uv = gl_FragCoord.xy / vec2(screenWidth, screenHeight);
    float X = uv.x * 10. + timer * (0.7 - getDepth());
    float Y = uv.y * 10. + timer * (0.6 - getDepth());
    uv.y += cos(X + Y) * 0.007 * cos(Y);
    uv.x += sin(X - Y) * 0.007 * sin(Y);

    reflectionCoords = vec2(uv.x, -uv.y);
    refractionCoords = uv;

    // Sample reflection and refraction textures
    vec4 reflectionColor = texture(reflectionTexture, reflectionCoords);
    vec4 refractionColor = texture(refractionTexture, refractionCoords);
    //vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;

    // Calculate fresnel (reflection/refraction mix depending on viewing angle)
    float fresnel = clamp(pitch / 90.0, 0.2f, 0.45f);

    //reflectionColor = mix(reflectionColor, vec4(0.010, 0.010, 0.43, 1.0), 0.3);
    vec4 color = mix(reflectionColor, refractionColor, fresnel);
    color = mix(color, vec4(0.010, 0.010, 0.43, 1.0), 0.5);

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}

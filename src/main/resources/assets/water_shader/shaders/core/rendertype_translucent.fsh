#version 150 core

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform sampler2D reflectionTexture;
uniform sampler2D refractionTexture;

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
in vec4 clipSpace;
in vec3 toCameraVector;

uniform float pitch;
uniform float timer;
uniform vec3 skyColor;
uniform float sneakOffset;

out vec4 fragColor;

float getDepth() {
    float near = 0.1;
    float far  = 1000.0;
    float z = gl_FragCoord.z * 2.0 - 1.0; // back to NDC
    float depth = (2.0 * near * far) / (far + near - z * (far - near));
    depth = depth / far;

    return depth;
}

vec4 rgb_to_glsl(vec4 rgb) {
    return rgb / 255.0;
}

float mapRange(float x, float a, float b, float c, float d) {
    // Map x from range [a, b] to range [c, d]
    return c + (d - c) * (x - a) / (b - a);
}

void main() {
    // Distortion
    float minFrequency = 50.0;
    float maxFrequency = 20.0;
    float minDistortionAmount = 0.002;
    float maxDistortionAmount = 0.009;
    float minwsEffet = 1.0;
    float maxwsEffet = 10.9;

    float frequency = mix(minFrequency, maxFrequency, getDepth());
    float distortionAmount = mix(minDistortionAmount, maxDistortionAmount, getDepth());
    float wsEffet = mix(minwsEffet, maxwsEffet, getDepth());

    // vec2 uv = gl_FragCoord.xy / vec2(screenWidth, screenHeight);
    vec2 uv = (clipSpace.xy / clipSpace.w) / 2.0 + 0.5;
    float X = uv.x * frequency + timer * 0.05;
    float Y = uv.y * frequency + timer * 0.05;
    uv.y += cos(X + Y) * distortionAmount * cos(Y * wsEffet);
    uv.x += sin(X - Y) * distortionAmount * sin(Y * wsEffet);

    // Calculate reflection and refraction texture coordinates
    vec2 reflectionCoords = vec2(uv.x, -uv.y);
    vec2 refractionCoords = uv;

    // Sample reflection and refraction textures
    vec4 reflectionColor = texture(reflectionTexture, reflectionCoords) * 0.70;
    vec4 refractionColor = texture(refractionTexture, refractionCoords);

    reflectionColor.a = 0.35;

    // Calculate fresnel (reflection/refraction mix depending on viewing angle)
    vec3 viewVector = normalize(toCameraVector);
    float reflectionFactor = clamp(dot(viewVector, vec3(0, 1, 0)), 0.2, 0.8);
    vec4 waterTint = vec4(240, 248, 255, 30);

    if (worldPos.y - sneakOffset > 61.4 || worldPos.y - sneakOffset < 60.0) {
        // Use refraction only
        float yLimit = 62.6 - sneakOffset;
        float y = worldPos.y;
        // reflectionFactor = mix(0.0, 1.0, 1 - min((yLimit - y) / yLimit, 1.0));
        reflectionFactor = clamp(mapRange(y, 61.3, 62.0, 0.0, 1.0), 0.0, 0.8);
        reflectionColor = mix(refractionColor, vec4(skyColor, 0.08), reflectionFactor);
    }

    vec4 color = mix(reflectionColor, refractionColor, reflectionFactor);
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}

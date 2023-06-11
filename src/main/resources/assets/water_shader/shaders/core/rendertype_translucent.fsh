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
in mat4 InverseProjMatrix;

uniform float pitch;
uniform float timer;
uniform float waveStrength;
uniform mat4 InverseViewMatrix;

out vec4 fragColor;

float getDepth() {
    float near = 0.1;
    float far  = 1000.0;
    float z = gl_FragCoord.z * 2.0 - 1.0; // back to NDC
    float depth = (2.0 * near * far) / (far + near - z * (far - near));
    depth = depth / far;

    return depth;
}

void main() {
    // Distortion
    float minFrequency = 60;
    float maxFrequency = 30;
    float minDistortionAmount = 0.001;
    float maxDistortionAmount = 0.003;
    float minwsEffet = 1.0;
    float maxwsEffet = 7.5;

    float frequency = mix(minFrequency, maxFrequency, getDepth());
    float distortionAmount = mix(minDistortionAmount, maxDistortionAmount, getDepth());
    float wsEffet = mix(minwsEffet, maxwsEffet, getDepth());

    vec2 uv = gl_FragCoord.xy / vec2(screenWidth, screenHeight);
    float X = uv.x * frequency + timer * 0.05;
    float Y = uv.y * frequency + timer * 0.05;
    uv.y += cos(X + Y) * distortionAmount * cos(Y * wsEffet);
    uv.x += sin(X - Y) * distortionAmount * sin(Y * wsEffet);

    // Calculate reflection and refraction texture coordinates
    vec2 reflectionCoords = vec2(uv.x, -uv.y);
    vec2 refractionCoords = uv;

    // Sample reflection and refraction textures
    vec4 reflectionColor = texture(reflectionTexture, reflectionCoords);
    vec4 refractionColor = texture(refractionTexture, refractionCoords);

    // Calculate fresnel (reflection/refraction mix depending on viewing angle)
    float fresnel = clamp(pitch / 90.0, 0.2f, 0.45f);

    //reflectionColor = mix(reflectionColor, vec4(0.010, 0.010, 0.43, 1.0), 0.3);
    vec4 color = mix(reflectionColor, refractionColor, fresnel);
    color = mix(color, vec4(0.001, 0.0003, 0.3, 0.85), 0.5);

    if (worldPos.y > 62) {
        color = vec4(0, 0, 1, 1);
    }
    else {
        color = vec4(0, 1, 0, 1);
    }

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);

//    color = vec4(vec3(worldPos.y), 1.0);
//    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);

//    vec4 ndc = vec4(
//    (gl_FragCoord.x / screenWidth - 0.5) * 2.0,
//    (gl_FragCoord.y / screenHeight - 0.5) * 2.0,
//    (gl_FragCoord.z - 0.5) * 2.0,
//    1.0);

//    if (worldPos.y < 1.0) {
//        color = vec4(1.0, 0.0, 0.0, 1.0);
//    }
//    else {
//        color = vec4(0.0, 1.0, 0.0, 1.0);
//    }

    //color = vec4(vec3(abs(worldPos.y)), 1.0);

    // Convert NDC throuch inverse clip coordinates to view coordinates
    //vec4 clip = InverseProjMatrix * ndc;
//    vec3 vertex = (clip / clip.w).xyz;

//    color = vec4(vertex, 1.0);
//    if (vertex.x > 2)
//        fragColor = vec4(1.0, 0.0, 0.0, 1.0);
//    else if (vertex.x < -2)
//        fragColor = vec4(0.0, 1.0, 0.0, 1.0);

//    color = vec4(1.0, 0.0, 0.0, 1.0);
    //fragColor = color;
    //fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}

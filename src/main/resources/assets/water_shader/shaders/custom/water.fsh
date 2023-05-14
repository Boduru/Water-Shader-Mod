#version 330 core

in vec2 TexCoords;

out vec4 outColor;

uniform sampler2D worldTexture;
uniform sampler2D reflectionTexture;
//uniform sampler2D refractionTexture;

bool isWater(vec4 color) {
    float epsilon = 0.1f;
    return (color.r >= (1.0f - epsilon)) && (color.g <= epsilon) && (color.b >= (1.0f - epsilon));
}

void main() {
    vec4 worldColor = texture(worldTexture, TexCoords);
//    vec4 reflectionColor = texture(reflectionTexture, vec2(TexCoords.x, -TexCoords.y));
    vec4 reflectionColor = texture(reflectionTexture, -TexCoords);

//    if (isWater(texColor)) {
//        texColor = texture(reflectionTexture, TexCoords);
//    }
//    vec4 texColor = texture(texture2, TexCoords);
//    vec4 lightBlue = vec4(0.2f, 0.3f, 0.75f, 0.85f);
//
//    vec4 color = mix(texColor, lightBlue, 0.45f);

//    if (isWater(worldColor)) {
//        outColor = vec4(0.0f, 0.0f, 1.0f, 1.0f);
//    }
//    else {
//        outColor = worldColor;
//    }

    if (isWater(worldColor))
        outColor = reflectionColor;
    else
        outColor = worldColor;
//    outColor = mix(worldColor, reflectionColor, 0.5f);

//    outColor = reflectionColor;
//    outColor = worldColor;


//    outColor = vec4(0.0f, 0.0f, 1.0f, 1.0f);
}
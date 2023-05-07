#version 150

in vec2 TexCoords;

out vec4 outColor;

uniform sampler2D texture1;

void main() {
    vec4 texColor = texture(texture1, TexCoords);
    vec4 lightBlue = vec4(0.2f, 0.3f, 0.75f, 0.85f);

    vec4 color = mix(texColor, lightBlue, 0.45f);

    outColor = color;
}
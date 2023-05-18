#version 150

uniform vec4 ColorModulator;

out vec4 fragColor;

void main() {
    fragColor = ColorModulator;// * vec4(0.2);
}

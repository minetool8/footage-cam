#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;

uniform float Strength;

// Matrix that takes a pixel of this frame (clip space + depth) to the same spot in the previous frame.
// Built on the CPU, uploaded as 16 floats (column-major: Mcolumn row).
uniform float M00; uniform float M01; uniform float M02; uniform float M03;
uniform float M10; uniform float M11; uniform float M12; uniform float M13;
uniform float M20; uniform float M21; uniform float M22; uniform float M23;
uniform float M30; uniform float M31; uniform float M32; uniform float M33;

in vec2 texCoord;
out vec4 fragColor;

const int SAMPLES = 16;
const float MAX_VELOCITY = 0.12;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec4 center = texture(DiffuseSampler, texCoord);

    mat4 reproject = mat4(
        M00, M01, M02, M03,
        M10, M11, M12, M13,
        M20, M21, M22, M23,
        M30, M31, M32, M33
    );

    float depth = texture(DiffuseDepthSampler, texCoord).r;
    vec4 prevClip = reproject * vec4(texCoord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);

    if (abs(prevClip.w) < 0.000001) {
        fragColor = center;
        return;
    }

    vec2 prevUv = prevClip.xy / prevClip.w * 0.5 + 0.5;
    vec2 velocity = texCoord - prevUv;

    float len = length(velocity);
    if (len > MAX_VELOCITY) {
        velocity *= MAX_VELOCITY / len;
    }
    velocity *= Strength * 0.5;

    float jitter = hash(gl_FragCoord.xy) - 0.5;
    vec4 sum = vec4(0.0);
    for (int i = 0; i < SAMPLES; i++) {
        float t = (float(i) + jitter) / float(SAMPLES - 1) * 2.0 - 1.0;
        sum += texture(DiffuseSampler, texCoord + velocity * t);
    }

    fragColor = sum / float(SAMPLES);
}

precision mediump float;

uniform sampler2D u_Texture;    // The input texture.
uniform vec2 u_Dimensions;
uniform bool u_Vertical;

void main(void) {
    vec4 color = texture2D(u_Texture, vec2(gl_FragCoord) / u_Dimensions);
    float alpha = color.a;
    if (alpha < 0.001) {
        discard;
    }

    float offsets[3];
    offsets[0] = 0.0;
    offsets[1] = 1.3846153846;
    offsets[2] = 3.2307692308;
    float weight[3];
    weight[0] = 0.2270270270;
    weight[1] = 0.3162162162;
    weight[2] = 0.0702702703;

    color *= weight[0];
    for (int i = 1; i < 3; i++) {
        vec2 offset;
        if (u_Vertical) {
            offset = vec2(0.0, offsets[i]);
        } else {
            offset = vec2(offsets[i], 0.0);
        }
        color +=
            texture2D(u_Texture, (vec2(gl_FragCoord) + offset) / u_Dimensions)
                * weight[i];
        color +=
            texture2D(u_Texture, (vec2(gl_FragCoord) - offset) / u_Dimensions)
                * weight[i];
    }
    gl_FragColor = vec4(vec3(color), alpha);
//    gl_FragColor = color;
}

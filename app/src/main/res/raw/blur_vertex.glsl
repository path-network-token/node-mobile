uniform mat4 u_ModelViewMatrix;

attribute vec4 a_Position;      // Per-vertex position information we will pass in.
attribute vec2 a_TexCoord;     // Per-vertex texture coordinate information we will pass in.

varying vec2 v_TexCoordinate;   // This will be passed into the fragment shader.

void main() {
    v_TexCoordinate = a_TexCoord;

    gl_Position = u_ModelViewMatrix * a_Position;
}

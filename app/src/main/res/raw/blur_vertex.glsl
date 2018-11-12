uniform mat4 u_ModelViewMatrix;

attribute vec4 a_Position;      // Per-vertex position information we will pass in.

void main() {
    gl_Position = u_ModelViewMatrix * a_Position;
}

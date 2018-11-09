uniform mat4 u_ModelViewMatrix;
uniform mat4 u_ProjectionMatrix;

attribute vec4 a_Position;      // Per-vertex position information we will pass in.
attribute vec3 a_Normal;        // Per-vertex normal information we will pass in.
attribute vec2 a_TexCoord;      // Per-vertex texture coordinate information we will pass in.

varying vec3 v_Position;        // This will be passed into the fragment shader.
varying vec3 v_Normal;          // This will be passed into the fragment shader.
varying vec2 v_TexCoordinate;   // This will be passed into the fragment shader.

// The entry point for our vertex shader.
void main() {
    // Pass through the texture coordinate.
    v_TexCoordinate = a_TexCoord;

    // Transform the vertex into eye space.
    v_Position = vec3(u_ModelViewMatrix * a_Position);

    // Transform the normal's orientation into eye space.
    v_Normal = vec3(u_ModelViewMatrix * vec4(a_Normal, 0.0));

    // gl_Position is a special variable used to store the final position.
    // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
    gl_Position = u_ProjectionMatrix * u_ModelViewMatrix * a_Position;
}

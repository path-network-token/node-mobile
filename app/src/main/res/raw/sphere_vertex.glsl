uniform mat4 u_ModelViewMatrix;
uniform mat4 u_ProjectionMatrix;
uniform float u_PointScale;

attribute vec4 a_Position;      // Per-vertex position information we will pass in.
attribute vec3 a_Normal;        // Per-vertex normal information we will pass in.
attribute vec4 a_Color;         // Per-vertex color information we will pass in.

varying vec3 v_Position;        // This will be passed into the fragment shader.
varying vec3 v_Normal;          // This will be passed into the fragment shader.
varying vec4 v_Color;

// The entry point for our vertex shader.
void main() {
    // Pass through the color.
    v_Color = a_Color;

    // Transform the vertex into eye space.
    v_Position = vec3(u_ModelViewMatrix * a_Position);

    // Transform the normal's orientation into eye space.
    v_Normal = vec3(u_ModelViewMatrix * vec4(a_Normal, 0.0));

    // gl_Position is a special variable used to store the final position.
    // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
    gl_Position = u_ProjectionMatrix * u_ModelViewMatrix * a_Position;

    vec3 ndc = gl_Position.xyz / gl_Position.w; // perspective divide.
    float zDist = 1.0 - ndc.z; // 1 is close (right up in your face,)
                               // 0 is far (at the far plane)
    gl_PointSize = 15.0 * u_PointScale; // between 0 and 50 now.
}

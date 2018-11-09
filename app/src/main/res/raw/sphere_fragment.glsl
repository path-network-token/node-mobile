precision mediump float;        // Set the default precision to medium. We don't need as high of a
                                // precision in the fragment shader.
uniform sampler2D u_Texture;    // The input texture.
uniform vec3 u_CameraPosition;

varying vec3 v_Position;        // Interpolated position for this fragment.
varying vec3 v_Normal;          // Interpolated normal for this fragment.
varying vec4 v_Color;

struct Material {
    float shininess;
    float diffuse;
    float specular;
};
uniform Material u_Material;

struct DirLight {
    vec3 direction;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};
uniform DirLight u_DirLight;

struct PointLight {
    vec3 position;

    float constant;
    float linear;
    float quadratic;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};
uniform PointLight u_Light;

uniform bool u_Point;
uniform bool u_DrawTop;

uniform float u_Near;
uniform float u_Far;

float EyePosition(float z, float n, float f) {
    float z_ndc = 2.0 * z - 1.0;
    float z_eye = 2.0 * n * f / (f + n - z_ndc * (f - n));
    return -z_eye;
}

vec3 CalcDirLight(DirLight light, vec3 normal, vec3 viewDir) {
    vec3 lightDir = normalize(-light.direction);
    // Diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // Specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), u_Material.shininess);
    // Combine results
    vec3 ambient  = light.ambient  * u_Material.diffuse; // * vec3(texture(material.diffuse, TexCoords));
    vec3 diffuse  = light.diffuse  * diff * u_Material.diffuse; // * vec3(texture(material.diffuse, TexCoords));
    vec3 specular = light.specular * spec * u_Material.specular; // * vec3(texture(material.specular, TexCoords));
    return (ambient + diffuse + specular);
}

vec3 CalcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir) {
    vec3 lightDir = normalize(light.position - fragPos);
    // Diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // Specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), u_Material.shininess);
    // Attenuation
    float distance    = length(light.position - fragPos);
    float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));
    // Combine results
    vec3 ambient  = light.ambient  * u_Material.diffuse; // * vec3(texture(material.diffuse, TexCoords));
    vec3 diffuse  = light.diffuse  * diff * u_Material.diffuse; // * vec3(texture(material.diffuse, TexCoords));
    vec3 specular = light.specular * spec * u_Material.specular; // * vec3(texture(material.specular, TexCoords));
    ambient  *= attenuation;
    diffuse  *= attenuation;
    specular *= attenuation;
    return (ambient + diffuse + specular);
}

void main() {
    if (EyePosition(gl_FragCoord.z, u_Near, u_Far) < u_CameraPosition.z) {
        if (u_DrawTop) {
            discard;
        }
    } else if (!u_DrawTop) {
        discard;
    }

    float alpha = 1.0;
    if (u_Point) {
        vec2 circCoord = 2.0 * gl_PointCoord - 1.0;
        float d = dot(circCoord, circCoord);
        if (d > 1.0) {
            discard;
        }
//        alpha = 1.0 - d * d;
    }

    vec3 norm = normalize(v_Normal);
    vec3 viewDir = normalize(u_CameraPosition - v_Position);

    vec3 result = CalcDirLight(u_DirLight, norm, viewDir);
    result += CalcPointLight(u_Light, norm, v_Position, viewDir);

//    float distance = length(u_Light.pos - v_Position);
//    vec3 lightVector = normalize(u_Light.pos - v_Position);
//    float diffuse = max(u_Light.diffuseFactor, dot(v_Normal, lightVector)) * u_Light.power;
//    diffuse = diffuse * (1.0 / (1.0 + (u_Light.attenuationFactor * u_Light.attenuationFactor * distance * distance)));

    gl_FragColor = vec4(result, 1.0) * v_Color;
    if (u_Point) {
        gl_FragColor.a = alpha;
    }
}

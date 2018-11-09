package network.path.mobilenode.ui.opengl.models

import android.opengl.GLES20
import android.renderscript.Float3
import android.renderscript.Matrix4f
import network.path.mobilenode.ui.opengl.glutils.ShaderProgram
import network.path.mobilenode.ui.opengl.glutils.createBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

//data class Light(val pos: Matrix4f,
//                 val power: Float,
//                 val diffuseFactor: Float = 0.7f,
//                 val attenuationFactor: Float = 0.5f)

data class Material(
    val shininess: Float = 5.0f,
    val diffuse: Float = 1.0f,
    val specular: Float = 1.0f
)

data class DirLight(
    val dir: Float3,
    val ambient: Float = 0.2f,
    val diffuse: Float = 0.2f,
    val specular: Float = 1.0f
)

data class PointLight(
    val pos: Matrix4f,
    val constant: Float = 1f,
    val linear: Float = 0.35f,
    val quadratic: Float = 0.44f,
    val ambient: Float = 0.2f,
    val diffuse: Float = 0.2f,
    val specular: Float = 1.0f
)
//7	1.0	0.7	1.8
//13	1.0	0.35	0.44
//20	1.0	0.22	0.20
//32	1.0	0.14	0.07
//50	1.0	0.09	0.032
//65	1.0	0.07	0.017
//100	1.0	0.045	0.0075
//160	1.0	0.027	0.0028
//200	1.0	0.022	0.0019
//325	1.0	0.014	0.0007
//600	1.0	0.007	0.0002
//3250	1.0	0.0014	0.000007

/**
 * Created by burt on 2016. 6. 22..
 */
open class Model(
        protected val name: String,
        protected val shader: ShaderProgram,
        protected val hasLight: Boolean,
        protected val provider: DataProvider
) {
    companion object {
        const val COORDS_PER_VERTEX = 3
        const val COLORS_PER_VERTEX = 4
        const val TEXCOORDS_PER_VERTEX = 2
        const val NORMALS_PER_VERTEX = 3

        const val SIZE_OF_FLOAT = 4
        const val SIZE_OF_SHORT = 2

        const val U_PROJECTION_NAME = "u_ProjectionMatrix"
        const val U_MODELVIEW_NAME = "u_ModelViewMatrix"
        const val A_POSITION_NAME = "a_Position"
        const val A_COLOR_NAME = "a_Color"
        const val U_TEXTURE_NAME = "u_Texture"
        const val A_TEXCOORD_NAME = "a_TexCoord"
        const val A_NORMAL_NAME = "a_Normal"
        const val U_LIGHT_NAME = "u_Light"
    }

    interface DataProvider {
        val vertices: FloatArray
        val indices: ShortArray

        val hasColor: Boolean
        val hasTexture: Boolean
        val hasNormals: Boolean
    }

    var textureHandle: Int = 0

    private var vertexBuffer: FloatBuffer? = null
    private var vertexBufferId: Int = 0
    private var vertexStride: Int = 0

    private var indexBuffer: ShortBuffer? = null
    private var indexBufferId: Int = 0

    // ModelView Transformation
    var position = Float3(0f, 0f, 0f)
    val rotation = Float3(0f, 0f, 0f)
    var rotationX = 0.0f
        set(value) {
            field = value
            if (field > 360f) {
                field -= 360f
            }
            if (field < 0f) {
                field += 360f
            }
        }

    var rotationY = 0.0f
        set(value) {
            field = value
            if (field > 360f) {
                field -= 360f
            }
            if (field < 0f) {
                field += 360f
            }
        }

    var rotationZ = 0.0f
        set(value) {
            field = value
            if (field > 360f) {
                field -= 360f
            }
            if (field < 0f) {
                field += 360f
            }
        }

    var scale = Float3(1f, 1f, 1f)

    var material = Material()
    var light: PointLight? = null
    var dirLight: DirLight? = null

    protected val camera = Matrix4f()
    private val projection = Matrix4f()

    init {
        setupVertexBuffer()
        setupIndexBuffer()

        var elementsCount = COORDS_PER_VERTEX
        if (provider.hasColor) {
            elementsCount += COLORS_PER_VERTEX
        }
        if (provider.hasTexture) {
            elementsCount += TEXCOORDS_PER_VERTEX
        }
        if (provider.hasNormals) {
            elementsCount += NORMALS_PER_VERTEX
        }
        vertexStride = elementsCount * SIZE_OF_FLOAT
    }

    private fun setupVertexBuffer() {
        val vertices = provider.vertices.copyOf()
        vertexBuffer = createBuffer(vertices)

        val buffer = IntBuffer.allocate(1)
        GLES20.glGenBuffers(1, buffer)
        vertexBufferId = buffer.get(0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.size * SIZE_OF_FLOAT, vertexBuffer, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    private fun setupIndexBuffer() {
        val indices = provider.indices.copyOf()
        indexBuffer = createBuffer(indices)

        val buffer = IntBuffer.allocate(1)
        GLES20.glGenBuffers(1, buffer)
        indexBufferId = buffer.get(0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBufferId)
        GLES20.glBufferData(
            GLES20.GL_ELEMENT_ARRAY_BUFFER,
            indices.size * SIZE_OF_SHORT,
            indexBuffer,
            GLES20.GL_STATIC_DRAW
        )
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    open fun modelMatrix() = Matrix4f().also {
        it.translate(position.x, position.y, position.z)
        it.rotate(rotationX, 1.0f, 0.0f, 0.0f)
        it.rotate(rotationY, 0.0f, 1.0f, 0.0f)
        it.rotate(rotationZ, 0.0f, 0.0f, 1.0f)
        it.scale(scale.x, scale.y, scale.z)
    }

    fun setScale(scale: Float) {
        this.scale.x = scale
        this.scale.y = scale
        this.scale.z = scale
    }

    open fun setCamera(mat: Matrix4f) = camera.load(mat)

    open fun setProjection(mat: Matrix4f) = projection.load(mat)

    protected open fun setValues() {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBufferId)

        shader.setUniformMatrix(U_PROJECTION_NAME, projection)

        val view = Matrix4f()
        view.load(camera)
        view.multiply(modelMatrix())
        shader.setUniformMatrix(U_MODELVIEW_NAME, view)

        var offset = 0
        shader.enableVertexAttribute(A_POSITION_NAME)
        shader.setVertexAttribute(
            A_POSITION_NAME,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            offset
        )
        offset += COORDS_PER_VERTEX * SIZE_OF_FLOAT

        if (provider.hasColor) {
            shader.enableVertexAttribute(A_COLOR_NAME)
            shader.setVertexAttribute(
                A_COLOR_NAME,
                COLORS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                offset
            )
            offset += COLORS_PER_VERTEX * SIZE_OF_FLOAT
        }

        if (provider.hasTexture) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
            shader.setUniformi(U_TEXTURE_NAME, 1)

            shader.enableVertexAttribute(A_TEXCOORD_NAME)
            shader.setVertexAttribute(
                A_TEXCOORD_NAME,
                TEXCOORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                offset
            )
            offset += TEXCOORDS_PER_VERTEX * SIZE_OF_FLOAT
        }

        if (provider.hasNormals) {
            shader.enableVertexAttribute(A_NORMAL_NAME)
            shader.setVertexAttribute(
                A_NORMAL_NAME,
                NORMALS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                offset
            )
            offset += NORMALS_PER_VERTEX * SIZE_OF_FLOAT
        }

        if (hasLight) {
            setMaterial(material)
            setDirLight(dirLight ?: DirLight(Float3(), 0f))
            setLight(light ?: PointLight(Matrix4f(), 0f))
        }
    }

    protected open fun doDraw() {
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, provider.indices.size, GLES20.GL_UNSIGNED_SHORT, 0)
    }

    protected open fun clearValues() {
        shader.disableVertexAttribute(A_POSITION_NAME)
        if (provider.hasColor) {
            shader.disableVertexAttribute(A_COLOR_NAME)
        }
        if (provider.hasTexture) {
            shader.disableVertexAttribute(A_TEXCOORD_NAME)
        }
        if (provider.hasNormals) {
            shader.disableVertexAttribute(A_NORMAL_NAME)
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    protected fun setMaterial(material: Material, attrName: String = "u_Material") {
        shader.setUniformf("$attrName.shininess", material.shininess)
        shader.setUniformf("$attrName.diffuse", material.diffuse)
        shader.setUniformf("$attrName.specular", material.specular)
    }

    protected fun setDirLight(light: DirLight, attrName: String = "u_DirLight") {
        shader.setUniformf("$attrName.direction", light.dir.x, light.dir.y, light.dir.z)
        shader.setUniformf(
            "$attrName.ambient",
            light.ambient,
            light.ambient,
            light.ambient
        )
        shader.setUniformf(
            "$attrName.diffuse",
            light.diffuse,
            light.diffuse,
            light.diffuse
        )
        shader.setUniformf(
            "$attrName.specular",
            light.specular,
            light.specular,
            light.specular
        )
    }

    protected open fun setLight(light: PointLight, attrName: String = U_LIGHT_NAME) {
        val lightPos = Matrix4f().also {
            it.load(light.pos)
        }

        val array = lightPos.array
        shader.setUniformf("$attrName.position", array[12], array[13], array[14])
        shader.setUniformf("$attrName.constant", light.constant)
        shader.setUniformf("$attrName.linear", light.linear)
        shader.setUniformf("$attrName.quadratic", light.quadratic)
        shader.setUniformf(
            "$attrName.ambient",
            light.ambient,
            light.ambient,
            light.ambient
        )
        shader.setUniformf(
            "$attrName.diffuse",
            light.diffuse,
            light.diffuse,
            light.diffuse
        )
        shader.setUniformf(
            "$attrName.specular",
            light.specular,
            light.specular,
            light.specular
        )
    }

    open fun draw(dt: Long) {
        shader.begin()

        setValues()
        doDraw()
        clearValues()

        val l = shader.getLog()
        if (l.isNotBlank()) {
            android.util.Log.d("TEST", "!!!! $l")
        }
        shader.end()
    }
}

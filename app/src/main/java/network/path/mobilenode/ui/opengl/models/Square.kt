package network.path.mobilenode.ui.opengl.models

import android.renderscript.Int2
import network.path.mobilenode.ui.opengl.glutils.ShaderProgram

open class Square(shader: ShaderProgram, name: String = "square") : Model(
    name, shader, false, SquareDataProvider()
) {

    var dimensionsScale: Float = 1f
    var dimensions: Int2 = Int2(0, 0)

    override fun setValues() {
        super.setValues()

        shader.setUniformf("u_Dimensions", dimensions.x * dimensionsScale, dimensions.y * dimensionsScale)
    }

    private class SquareDataProvider : DataProvider {
        override val vertices = floatArrayOf(
            -1.0f,  1.0f, 0.0f, 0.0f, 1.0f, // top left
            -1.0f, -1.0f, 0.0f, 0.0f, 0.0f, // bottom left
             1.0f, -1.0f, 0.0f, 1.0f, 0.0f, // bottom right
             1.0f,  1.0f, 0.0f, 1.0f, 1.0f // top right
        )

        override val indices = shortArrayOf(0, 1, 2, 0, 2, 3)

        override val hasColor = false
        override val hasTexture = true
        override val hasNormals = false
    }
}

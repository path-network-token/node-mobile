package network.path.mobilenode.ui.opengl.models

import android.content.Context
import android.opengl.GLES20
import network.path.mobilenode.ui.opengl.glutils.ObjLoader
import network.path.mobilenode.ui.opengl.glutils.ShaderProgram
import network.path.mobilenode.ui.opengl.models.providers.ObjDataProvider

class ObjSphere(shader: ShaderProgram, context: Context, modelPath: String, radius: Float, color: FloatArray) :
    Model(
        "globe", shader, true,
        ObjDataProvider(
            ObjLoader(
                context,
                modelPath,
                true,
                radius,
                color,
                false
            )
        )
    ) {

    override fun doDraw() {
        val size = provider.indices.size
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glLineWidth(2.0f)
        GLES20.glDrawElements(GLES20.GL_LINES, size, GLES20.GL_UNSIGNED_SHORT, 0)
        GLES20.glDrawElements(GLES20.GL_POINTS, size, GLES20.GL_UNSIGNED_SHORT, 0)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
    }
}

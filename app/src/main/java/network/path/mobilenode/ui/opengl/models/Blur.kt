package network.path.mobilenode.ui.opengl.models

import android.opengl.GLES20
import network.path.mobilenode.ui.opengl.glutils.ShaderProgram

class Blur(shader: ShaderProgram) : Square(shader, "blur") {
    var isVertical = false

    override fun setValues() {
        super.setValues()

        shader.setUniformf("u_Vertical", if (isVertical) 1f else 0f)
    }

    override fun doDraw() {
        val isEnabled = GLES20.glIsEnabled(GLES20.GL_CULL_FACE)
        if (isEnabled) {
            GLES20.glDisable(GLES20.GL_CULL_FACE)
        }
        super.doDraw()
        if (isEnabled) {
            GLES20.glEnable(GLES20.GL_CULL_FACE)
        }
    }
}

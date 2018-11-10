package network.path.mobilenode.ui.opengl.models

import android.opengl.GLES20
import network.path.mobilenode.ui.opengl.glutils.ShaderProgram
import network.path.mobilenode.ui.opengl.models.providers.ObjDataProvider

class Globe(shader: ShaderProgram, provider: ObjDataProvider) :
        Model("globe", shader, true, provider) {

    var alpha = 0f
    var drawTop = false

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

    override fun setValues() {
        super.setValues()

        shader.setUniformf("u_Alpha", alpha)
        shader.setUniformf("u_Near", 1.0f)
        shader.setUniformf("u_Far", 100.0f)
        shader.setUniformf("u_CameraPosition", camera.get(1, 0), camera.get(2, 1), camera.get(3, 2))
        shader.setUniformf("u_DrawTop", if (drawTop) 1.0f else 0.0f)
    }
}


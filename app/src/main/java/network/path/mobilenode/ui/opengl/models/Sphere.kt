package network.path.mobilenode.ui.opengl.models

import android.opengl.GLES20
import network.path.mobilenode.ui.opengl.glutils.ShaderProgram
import network.path.mobilenode.ui.opengl.models.providers.SphereDataProvider

class Sphere(shader: ShaderProgram, provider: SphereDataProvider) :
    Model("sphere", shader, true, provider) {

    var alpha = 0f
    var drawTop = false
    var pointScale = 1f

    override fun setValues() {
        super.setValues()

        GLES20.glLineWidth(2f)
        shader.setUniformf("u_Near", 1f)
        shader.setUniformf("u_Far", 100f)
        shader.setUniformf("u_Alpha", alpha)
        shader.setUniformf("u_CameraPosition", camera.get(1, 0), camera.get(2, 1), camera.get(3, 2))
        shader.setUniformf("u_DrawTop", if (drawTop) 1f else 0f)
        shader.setUniformf("u_PointScale", if (drawTop) 1f else pointScale)
    }

    override fun doDraw() {
        val size = provider.indices.size
        shader.setUniformf("u_Point", 0f)
        GLES20.glDrawElements(GLES20.GL_LINES, size, GLES20.GL_UNSIGNED_SHORT, 0)
        shader.setUniformf("u_Point", 1f)
        GLES20.glDrawElements(GLES20.GL_POINTS, size, GLES20.GL_UNSIGNED_SHORT, 0)
    }
}

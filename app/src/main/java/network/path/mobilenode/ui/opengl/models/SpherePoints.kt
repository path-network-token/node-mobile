package network.path.mobilenode.ui.opengl.models

import android.opengl.GLES20
import network.path.mobilenode.ui.opengl.glutils.ShaderProgram
import network.path.mobilenode.ui.opengl.models.providers.SphereDataProvider

class SpherePoints(shader: ShaderProgram, provider: SphereDataProvider) :
    Model("spherePoints", shader, true, provider) {

    override fun setValues() {
        super.setValues()
        shader.setUniformf("u_Point", 1.0f)
    }

    override fun doDraw() {
        val size = provider.indices.size
        GLES20.glDrawElements(GLES20.GL_POINTS, size, GLES20.GL_UNSIGNED_SHORT, 0)
    }
}

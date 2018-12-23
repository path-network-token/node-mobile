package network.path.mobilenode.ui.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import network.path.mobilenode.R
import network.path.mobilenode.domain.entity.ConnectionStatus

class OpenGLSurfaceView
@JvmOverloads constructor(context: Context, attrSet: AttributeSet? = null) : GLSurfaceView(context, attrSet) {
    private val renderer: OpenGLRenderer

    init {
        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2)

        // fix for error No Config chosen
        super.setEGLConfigChooser(8, 8, 8, 8, 16, 0)

        preserveEGLContextOnPause = true

        renderer = OpenGLRenderer(context)
        renderer.listener = object : OpenGLRenderer.Listener {
            override fun onInitialised() {
                setBackgroundResource(0)
            }
        }

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)

//        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    fun saveState() = renderer.saveState()

    fun restoreState(savedState: Bundle) {
        renderer.restoreState(savedState)
    }

    fun destroy() {
        renderer.destroy()
    }

    fun setConnectionStatus(status: ConnectionStatus) {
        renderer.setSphereColor(status.color())
    }

    fun setRunning(isRunning: Boolean) {
        renderer.toggleRotation(isRunning)
    }

    private fun ConnectionStatus.color(): Int = ContextCompat.getColor(context, when (this) {
        ConnectionStatus.CONNECTED -> R.color.light_teal
        ConnectionStatus.PROXY -> R.color.light_teal
        ConnectionStatus.LOOKING,
        ConnectionStatus.DISCONNECTED -> android.R.color.transparent
    })
}

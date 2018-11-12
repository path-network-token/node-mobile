package network.path.mobilenode.ui.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

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

    fun destroy() {
        renderer.destroy()
    }
}
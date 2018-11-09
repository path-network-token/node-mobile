package network.path.mobilenode.ui.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class MyGLSurfaceView
@JvmOverloads constructor(context: Context, attrSet: AttributeSet? = null) : GLSurfaceView(context, attrSet) {
    val renderer: MyGLRenderer

    init {
        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2)

        // fix for error No Config chosen
        super.setEGLConfigChooser(8, 8, 8, 8, 16, 0)

        preserveEGLContextOnPause = true

        renderer = MyGLRenderer(context)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)

//        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }
}

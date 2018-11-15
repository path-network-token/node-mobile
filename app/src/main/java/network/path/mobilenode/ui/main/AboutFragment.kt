package network.path.mobilenode.ui.main

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES20
import android.os.Build
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.dashboard_details.*
import kotlinx.android.synthetic.main.fragment_job_report.*
import network.path.mobilenode.BuildConfig
import network.path.mobilenode.R
import network.path.mobilenode.ui.base.BaseFragment

class AboutFragment : BaseFragment() {
    override val layoutResId = R.layout.fragment_about

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCloseScreenButton()
        populateData()
    }

    private fun setupCloseScreenButton() {
        closeScreenImageView.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun populateData() {
        label1.text = getString(R.string.label_app_version)
        label2.text = getString(R.string.label_os_version)
        label3.text = getString(R.string.label_gl_version)
        label4.text = getString(R.string.label_glsl_version)

        value1.text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        value2.text = "${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT} ${Build.VERSION.CODENAME})"

        setupGL()
    }

    private fun setupGL() {
        // EGL config attributes
        val confAttr = intArrayOf(
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,    // very important!
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,          // we will create a pixelbuffer surface
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,     // if you need the alpha channel
                EGL14.EGL_DEPTH_SIZE, 16,    // if you need the depth buffer
                EGL14.EGL_NONE
        )

        // EGL context attributes
        val ctxAttr = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,              // very important!
                EGL14.EGL_NONE
        )

        // surface attributes
        // the surface size is set to the input frame size
        val surfaceAttr = intArrayOf(EGL14.EGL_WIDTH, 640,
                EGL14.EGL_HEIGHT, 480,
                EGL14.EGL_NONE
        )

        val major = IntArray(1)
        val minor = IntArray(1)
        val eglDisp = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        EGL14.eglInitialize(eglDisp, major, 0, minor, 0)

        // choose the first config, i.e. best config
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        EGL14.eglChooseConfig(eglDisp, confAttr, 0, configs, 0, configs.size, numConfigs, 0)

        val eglConf = configs[0]
        val eglCtx = EGL14.eglCreateContext(eglDisp, eglConf, EGL14.EGL_NO_CONTEXT, ctxAttr, 0)

        // create a pixelbuffer surface
        val eglSurface = EGL14.eglCreatePbufferSurface(eglDisp, eglConf, surfaceAttr, 0)

        EGL14.eglMakeCurrent(eglDisp, eglSurface, eglSurface, eglCtx)

        value3.text = GLES20.glGetString(GLES20.GL_VERSION)
        value4.text = GLES20.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION)

        EGL14.eglMakeCurrent(eglDisp, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)

        EGL14.eglDestroyContext(eglDisp, eglCtx)
        EGL14.eglDestroySurface(eglDisp, eglSurface)
        EGL14.eglTerminate(eglDisp)
    }
}

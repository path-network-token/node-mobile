package network.path.mobilenode.ui.opengl

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.renderscript.Float3
import android.renderscript.Int2
import android.renderscript.Matrix4f
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import network.path.mobilenode.R
import network.path.mobilenode.ui.opengl.glutils.ShaderProgram
import network.path.mobilenode.ui.opengl.models.Blur
import network.path.mobilenode.ui.opengl.models.DirLight
import network.path.mobilenode.ui.opengl.models.Globe
import network.path.mobilenode.ui.opengl.models.Material
import network.path.mobilenode.ui.opengl.models.Sphere
import network.path.mobilenode.ui.opengl.models.Square
import network.path.mobilenode.ui.opengl.models.providers.ObjDataProvider
import network.path.mobilenode.ui.opengl.models.providers.SphereDataProvider
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class FPSCounter {
    private var startTime = System.nanoTime()
    private var frames = 0

    fun logFrame() {
        frames++
        if (System.nanoTime() - startTime >= 1000000000) {
            Timber.d("FPS: $frames")
            frames = 0
            startTime = System.nanoTime()
        }
    }
}

class OpenGLRenderer(private val context: Context) : GLSurfaceView.Renderer, KoinComponent {
    companion object {
        private val FPS = FPSCounter()

        private const val FRAMEBUFFER_COUNT = 2
        private const val START_CAMERA_Z = -5f
        private const val FINAL_CAMERA_Z = -2.5f

        private const val COLOR_ANIMATION_DURATION = 3_000L
        private const val FIRST_ANIMATION_DURATION = 3_000L
        private const val OTHER_ANIMATION_DURATION = 1_000L
        private const val ROTATION_DURATION_GLOBE = 20_000L // One complete rotation of globe
        private const val ROTATION_DURATION_SPHERE = 15_000L // One complete rotation of globe

        private const val BLUR_SCALE = 0.5f

        private const val STATE_ZOOMED = "STATE_ZOOMED"
    }

    interface Listener {
        fun onInitialised()
    }

    private var zoomComplete: Boolean = false

    var listener: Listener? = null

    private val objDataProvider by inject<ObjDataProvider>()
    private val sphereDataProvider by inject<SphereDataProvider>()

    private var lastTimeNanos = 0L

    private lateinit var bg: Square
    private lateinit var globe: Globe
    private lateinit var sphere: Sphere

    private var sphereColor = ContextCompat.getColor(context, R.color.light_teal)
    private var sphereColorAnimator: Animator? = null

    private lateinit var blurHorizontal: Blur
    private lateinit var blurVertical: Blur

    private lateinit var size: Int2

    private val rotation = Matrix4f().also {
        it.rotate(22f, 0f, 0f, 1f)
        it.rotate(25f, 1f, 0f, 0f)
    }
    private val camera = Matrix4f()

    private var bgTexture: Int = 0
    private var globeTexture: Int = 0
    private val programs = mutableListOf<ShaderProgram>()

    override fun onSurfaceCreated(unused: GL10, p1: EGLConfig?) {
        Timber.d("GL_VERSION: ${GLES20.glGetString(GLES20.GL_VERSION)}")
        Timber.d("GL_SHADING_LANGUAGE_VERSION: ${GLES20.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION)}")

        val values = IntBuffer.allocate(4)
        GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_ATTRIBS, values)
        Timber.d("GL_MAX_VERTEX_ATTRIBS: ${values[0]}")

        GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_UNIFORM_VECTORS, values)
        Timber.d("GL_MAX_VERTEX_UNIFORM_VECTORS: ${values[0]}")

        GLES20.glGetIntegerv(GLES20.GL_MAX_VARYING_VECTORS, values)
        Timber.d("GL_MAX_VARYING_VECTORS: ${values[0]}")

        GLES20.glGetIntegerv(GLES20.GL_MAX_RENDERBUFFER_SIZE, values)
        Timber.d("GL_MAX_RENDERBUFFER_SIZE: ${values[0]}")

        GLES20.glGetIntegerv(GLES20.GL_ALIASED_POINT_SIZE_RANGE, values)
        Timber.d("GL_ALIASED_POINT_SIZE_RANGE: ${values[0]}, ${values[1]}")

        GLES20.glGetIntegerv(GLES20.GL_MAX_VIEWPORT_DIMS, values)
        Timber.d("GL_MAX_VIEWPORT_DIMS: ${values[0]}, ${values[1]} - ${values[2]}, ${values[3]}")

        // TEXTURES
        bgTexture = loadTexture(context, R.drawable.gradient_background, true)
        globeTexture = loadTexture(context, R.drawable.earth_texture)

        // Background
        val bgShader = ShaderProgram(
                loadRawResource(context, R.raw.bg_vertex),
                loadRawResource(context, R.raw.bg_fragment)
        )
        programs.add(bgShader)
        bg = Square(bgShader).also {
            it.textureHandle = bgTexture
        }

        // Wireframe sphere
        val sphereShader = ShaderProgram(
                loadRawResource(context, R.raw.sphere_vertex),
                loadRawResource(context, R.raw.sphere_fragment)
        )
        programs.add(sphereShader)
        sphere = Sphere(sphereShader, sphereDataProvider)

        // Globe
        val globeShader = ShaderProgram(
                loadRawResource(context, R.raw.globe_vertex),
                loadRawResource(context, R.raw.globe_fragment)
        )
        programs.add(globeShader)
        globe = Globe(globeShader, objDataProvider).also {
            it.textureHandle = globeTexture
        }

        val blurShader = ShaderProgram(
                loadRawResource(context, R.raw.blur_vertex),
                loadRawResource(context, R.raw.blur_fragment)
        )
        programs.add(blurShader)
        blurHorizontal = Blur(blurShader)
        blurHorizontal.isVertical = false

        blurVertical = Blur(blurShader)
        blurVertical.isVertical = true

        // Set initial positions
        val position = Float3(0f, 0f, 0f)
        sphere.position = position
        globe.position = position

        val blurPosition = Float3(0f, 0f, 0.9f)
        blurHorizontal.position = blurPosition
        blurVertical.position = blurPosition

        bg.position = Float3(0f, 0f, 0.99f)

        // Scale (otherwise sphere get cut but projection if positioned too close to the camera)
        sphere.setScale(1.1f)
        globe.setScale(1.1f)

        globe.material = Material(5f, 4f, 4f)
        sphere.material = Material(5f, 2f, 0.1f)

        // Light source
        val direction = Float3(0.8f, 0.8f, 2f)
        globe.dirLight = DirLight(direction, ambient = 0.5f)
        sphere.dirLight = DirLight(direction, ambient = 0.2f)

        lastTimeNanos = System.nanoTime()

        // Camera position (and zoom animation)
        if (zoomComplete) {
            camera.translate(0.0f, 0.2f, FINAL_CAMERA_Z)
            globe.alpha = 1f
            sphere.alpha = 1f
        } else {
            camera.translate(0.0f, 0.2f, START_CAMERA_Z)
            globe.alpha = 0f
            sphere.alpha = 0f
        }
        setCamera(camera)
        setupFirstAnimation(!zoomComplete)

        // Rotation animation
        setupRotateAnimation()
    }

    override fun onDrawFrame(unused: GL10) {
        FPS.logFrame()

        val currentTimeNanos = System.nanoTime()
        val dt = currentTimeNanos - lastTimeNanos
        lastTimeNanos = currentTimeNanos

        val clearColor = ContextCompat.getColor(context, android.R.color.transparent)

        GLES20.glViewport(0, 0, size.x / 2, size.y / 2)
        drawFrame(dt, clearColor, frameBufferIds[0]) {
            sphere.tint = sphereColor
            sphere.drawTop = false
            sphere.draw(dt)

            globe.drawTop = false
            globe.draw(dt)

            globe.drawTop = true
            globe.draw(dt)
        }

        drawFrame(dt, clearColor, frameBufferIds[1]) {
            blurHorizontal.textureHandle = frameTextureIds[0]
            blurHorizontal.draw(dt)
        }

        GLES20.glViewport(0, 0, size.x, size.y)
        drawFrame(dt, clearColor, 0) {
            bg.draw(dt)

            blurVertical.textureHandle = frameTextureIds[1]
            blurVertical.draw(dt)

            sphere.drawTop = true
            sphere.draw(dt)
        }
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        size = Int2(width, height)
        GLES20.glViewport(0, 0, width, height)
        prepareFrameBuffers(BLUR_SCALE)

        val ratio = width.toFloat() / height.toFloat()
        val perspective = Matrix4f().also {
            it.loadPerspective(85f, ratio, 1f, 100f)
        }

        sphere.setProjection(perspective)
        globe.setProjection(perspective)

        blurHorizontal.dimensions = size
        blurHorizontal.dimensionsScale = BLUR_SCALE

        blurVertical.dimensions = size
        blurVertical.dimensionsScale = 1f

        sphere.pointScale = BLUR_SCALE

        bg.dimensions = size

        runOnUiThread {
            listener?.onInitialised()
        }
    }

    fun destroy() {
        val textureHandle = IntArray(2)
        textureHandle[0] = bgTexture
        textureHandle[1] = globeTexture
        GLES20.glDeleteTextures(textureHandle.size, textureHandle, 0)

        GLES20.glDeleteFramebuffers(frameBufferIds.size, frameBufferIds, 0)
        GLES20.glDeleteTextures(frameTextureIds.size, frameTextureIds, 0)
        GLES20.glDeleteRenderbuffers(rboIds.size, rboIds, 0)

        programs.forEach { it.destroy() }
    }

    fun saveState(): Bundle {
        val state = Bundle()
        state.putBoolean(STATE_ZOOMED, zoomComplete)
        return state
    }

    fun restoreState(savedState: Bundle) {
        zoomComplete = savedState.getBoolean(STATE_ZOOMED, false)
    }

    fun setSphereColor(@ColorInt color: Int) {
        val animator = ValueAnimator.ofArgb(sphereColor, color)
        animator.duration = COLOR_ANIMATION_DURATION
        animator.interpolator = BounceInterpolator()
        animator.addUpdateListener {
            val progress = it.animatedValue as Int
            sphereColor = progress
        }
        runOnUiThread {
            sphereColorAnimator?.cancel()
            sphereColorAnimator = animator
            animator.start()
        }
    }

    private fun setupFirstAnimation(zoom: Boolean = true) {
        val duration = if (zoom) FIRST_ANIMATION_DURATION else OTHER_ANIMATION_DURATION
        val alphaAnimator = ValueAnimator.ofFloat(0f, 1f)
        alphaAnimator.interpolator = AccelerateDecelerateInterpolator()
        alphaAnimator.addUpdateListener {
            val progress = it.animatedValue as Float
            globe.alpha = progress
            sphere.alpha = progress
        }

        val cameraAnimator = ValueAnimator.ofFloat(START_CAMERA_Z, FINAL_CAMERA_Z)
        cameraAnimator.interpolator = AccelerateDecelerateInterpolator()
        cameraAnimator.addUpdateListener {
            val progress = it.animatedValue as Float
            val array = camera.array
            array[14] = progress
            setCamera(camera)
        }

        val animatorSet = AnimatorSet()
        animatorSet.duration = duration
        if (zoom) {
            animatorSet.play(alphaAnimator).with(cameraAnimator)
        } else {
            animatorSet.play(alphaAnimator)
        }
        animatorSet.addListener(onEnd = {
            zoomComplete = true
        })
        runOnUiThread { animatorSet.start() }
    }

    private fun setupRotateAnimation() {
        val animatorGlobe = ValueAnimator.ofFloat(0f, 1f)
        animatorGlobe.duration = ROTATION_DURATION_GLOBE
        animatorGlobe.repeatCount = Animation.INFINITE
        animatorGlobe.interpolator = LinearInterpolator()
        animatorGlobe.addUpdateListener {
            val progress = it.animatedValue as Float
            globe.rotationY = progress * 360f
        }

        val animatorSphere = ValueAnimator.ofFloat(0f, 1f)
        animatorSphere.duration = ROTATION_DURATION_SPHERE
        animatorSphere.repeatCount = Animation.INFINITE
        animatorSphere.interpolator = LinearInterpolator()
        animatorSphere.addUpdateListener {
            val progress = it.animatedValue as Float
            sphere.rotationY = progress * 360f
        }
        runOnUiThread {
            animatorGlobe.start()
            animatorSphere.start()
        }
    }

    private fun setCamera(camera: Matrix4f) {
        val newCamera = Matrix4f().also {
            it.load(camera)
            it.multiply(rotation)
        }

        globe.setCamera(newCamera)

        sphere.setCamera(camera)
    }

    private fun drawFrame(dt: Long, @ColorInt bgColor: Int, bufferId: Int = 0, drawBlock: (Long) -> Unit) {
        val color = colorToFloatArray(bgColor)

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, bufferId)
        GLES20.glClearColor(color[0], color[1], color[2], color[3])
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        drawBlock(dt)
    }

    private var frameBufferIds = IntArray(FRAMEBUFFER_COUNT)
    private var frameTextureIds = IntArray(FRAMEBUFFER_COUNT)
    private var rboIds = IntArray(FRAMEBUFFER_COUNT)

    private fun prepareFrameBuffers(scale: Float = 1f) {
        GLES20.glGenFramebuffers(FRAMEBUFFER_COUNT, frameBufferIds, 0)
        GLES20.glGenTextures(FRAMEBUFFER_COUNT, frameTextureIds, 0)
        GLES20.glGenRenderbuffers(FRAMEBUFFER_COUNT, rboIds, 0)

        (0 until FRAMEBUFFER_COUNT).forEach { index ->
            val frameBufferId = frameBufferIds[index]
            val frameTextureId = frameTextureIds[index]
            val rboId = rboIds[index]

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId)

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameTextureId)
            GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D,
                    0,
                    GLES20.GL_RGBA,
                    (size.x * scale).toInt(),
                    (size.y * scale).toInt(),
                    0,
                    GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE,
                    null
            )
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

            // attach it to currently bound framebuffer object
            GLES20.glFramebufferTexture2D(
                    GLES20.GL_FRAMEBUFFER,
                    GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D,
                    frameTextureId,
                    0
            )

            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, rboId)
            GLES20.glRenderbufferStorage(
                    GLES20.GL_RENDERBUFFER,
                    GLES20.GL_DEPTH_COMPONENT16,
                    (size.x * scale).toInt(),
                    (size.y * scale).toInt()
            )
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0)

            GLES20.glFramebufferRenderbuffer(
                    GLES20.GL_FRAMEBUFFER,
                    GLES20.GL_DEPTH_ATTACHMENT,
                    GLES20.GL_RENDERBUFFER,
                    rboId
            )

            val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
            if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                throw RuntimeException("Frame buffer is incomplete: $status")
            }

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        }
    }

    private fun runOnUiThread(block: () -> Unit) {
        Handler(Looper.getMainLooper()).post {
            block()
        }
    }
}

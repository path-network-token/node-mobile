package network.path.mobilenode.ui.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.renderscript.Float3
import android.renderscript.Float4
import android.renderscript.Int2
import android.renderscript.Matrix4f
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

class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer, KoinComponent {
    companion object {
        private val FPS = FPSCounter()

        private const val FRAMEBUFFER_COUNT = 2
        private const val START_CAMERA_Z = -5f
        private const val FINAL_CAMERA_Z = -2.5f

        private const val ROTATION_SPEED_GLOBE = 270f / 15_000_000_000f // One complete rotation per 15 secs
        private const val ROTATION_SPEED_SPHERE = 360f / 15_000_000_000f // One complete rotation per 15 secs
        private const val ZOOM_SPEED = 1f / 1_000_000_000f // One unit per second

        private val WIREFRAME_COLOR = floatArrayOf(0.47058824f, 0.8f, 0.87843137f, 1.0f)

        private const val BLUR_SCALE = 0.5f
    }

    interface Listener {
        fun onInitialised()
    }

    var listener: Listener? = null

    private val objDataProvider by inject<ObjDataProvider>()

    private var lastTimeNanos = 0L

    private lateinit var bg: Square
    private lateinit var globe: Globe
    private lateinit var sphere: Sphere

    private lateinit var blurHorizontal: Blur
    private lateinit var blurVertical: Blur

    private lateinit var size: Int2

    private val rotation = Matrix4f().also {
        it.rotate(22f, 0f, 0f, 1f)
        it.rotate(25f, 1f, 0f, 0f)
    }
    private val camera = Matrix4f()

    private var bgTexture: Int = 0
    private var firstDraw = true

    override fun onSurfaceCreated(unused: GL10, p1: EGLConfig?) {
        lastTimeNanos = System.nanoTime()

        // TEXTURES
        bgTexture = loadTexture(context, R.drawable.gradient_background)

        // Wireframe sphere
        val sphereShader = ShaderProgram(
                loadRawResource(context, R.raw.sphere_vertex),
                loadRawResource(context, R.raw.sphere_fragment)
        )
        val sphereProvider = SphereDataProvider(2, 1.1f, WIREFRAME_COLOR)
        sphere = Sphere(sphereShader, sphereProvider)

        // Globe
        val globeShader = ShaderProgram(
                loadRawResource(context, R.raw.globe_vertex),
                loadRawResource(context, R.raw.globe_fragment)
        )
        globe = Globe(globeShader, objDataProvider).also {
            it.textureHandle = loadTexture(context, R.drawable.earth3)
        }

        // Background
        val bgShader = ShaderProgram(
                loadRawResource(context, R.raw.bg_vertex),
                loadRawResource(context, R.raw.bg_fragment)
        )
        bg = Square(bgShader).also {
            it.textureHandle = bgTexture
        }

        val blurShader = ShaderProgram(
                loadRawResource(context, R.raw.blur_vertex),
                loadRawResource(context, R.raw.blur_fragment)
        )
        blurHorizontal = Blur(blurShader)
        blurHorizontal.isVertical = false

        blurVertical = Blur(blurShader)
        blurVertical.isVertical = true

        // Set initial positions
        val position = Float3(0f, 0.2f, 0f)
        sphere.position = position
        globe.position = position

        val blurPosition = Float3(0f, 0f, 0.99f)
        blurHorizontal.position = blurPosition
        blurVertical.position = blurPosition

        bg.position = Float3(0f, 0f, 1f)

        // Scale (otherwise sphere get cut but projection if positioned too close to the camera)
        sphere.setScale(1.1f)
        globe.setScale(1.1f)

        // Camera position
        camera.translate(0.0f, 0.0f, START_CAMERA_Z)
        setCamera(camera)

        globe.material = Material(5f, 5f, 5f)
        sphere.material = Material(5f, 3f, 0.1f)

        // Light source
        val direction = Float3(0.8f, 0.8f, 2f)
        globe.dirLight = DirLight(direction, ambient = 0.5f)
        sphere.dirLight = DirLight(direction, ambient = 0.2f)
    }

    override fun onDrawFrame(unused: GL10) {
        FPS.logFrame()

        val currentTimeNanos = System.nanoTime()
        val dt = currentTimeNanos - lastTimeNanos
        lastTimeNanos = currentTimeNanos

        val clearColor = Float4(0f, 0f, 0f, 0f)
        val blackColor = Float4(0f, 0f, 0f, 1f)
        if (firstDraw) {
            GLES20.glViewport(0, 0, size.x, size.y)
            drawFrame(dt, blackColor, 0) {
                bg.textureHandle = bgTexture
                bg.draw(dt)
            }
            firstDraw = false
        } else {
            val z = camera.get(3, 2)
            if (z < FINAL_CAMERA_Z) {
                val dz = dt * ZOOM_SPEED
                val newZ = Math.min(z + dz, FINAL_CAMERA_Z)
                camera.set(3, 2, newZ)
                setCamera(camera)
            }
            sphere.rotationY += dt * ROTATION_SPEED_SPHERE
            globe.rotationY += dt * ROTATION_SPEED_GLOBE

            GLES20.glViewport(0, 0, size.x / 2, size.y / 2)
            drawFrame(dt, clearColor, frameBufferIds[0]) {
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
            drawFrame(dt, blackColor, 0) {
                bg.textureHandle = bgTexture
                bg.draw(dt)

                blurVertical.textureHandle = frameTextureIds[1]
                blurVertical.draw(dt)

                sphere.drawTop = true
                sphere.draw(dt)
            }
        }
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        size = Int2(width, height)
        GLES20.glViewport(0, 0, width, height)
        prepareFrameBuffers(BLUR_SCALE)

        val ratio = width.toFloat() / height.toFloat()
        val perspective = Matrix4f().also {
            it.loadPerspective(85.0f, ratio, 1.0f, 100.0f)
        }

        sphere.setProjection(perspective)
        globe.setProjection(perspective)

        blurHorizontal.dimensions = size
        blurHorizontal.dimensionsScale = BLUR_SCALE

        blurVertical.dimensions = size
        blurVertical.dimensionsScale = 1f

        sphere.pointScale = BLUR_SCALE

        bg.dimensions = size

        Handler(Looper.getMainLooper()).post {
            listener?.onInitialised()
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

    private fun drawFrame(dt: Long, bgColor: Float4, bufferId: Int = 0, drawBlock: (Long) -> Unit) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, bufferId)
        GLES20.glClearColor(bgColor.x, bgColor.y, bgColor.z, bgColor.w)
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
}

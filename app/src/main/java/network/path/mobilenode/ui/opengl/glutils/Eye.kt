package network.path.mobilenode.ui.opengl.glutils

import android.opengl.Matrix
import android.renderscript.Float3
import android.renderscript.Matrix4f
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class Eye {
    var position: Float3 = Float3(0f, 0f, 0f)
    var worldUp: Float3 = Float3(0f, 1f, 0f)
    var front: Float3 = Float3(0f, 0f, -1f)

    private lateinit var right: Float3
    private lateinit var up: Float3

    val matrix: Matrix4f
        get() {
            val arr = FloatArray(16)
            val target = position.add(front)
            Matrix.setLookAtM(arr, 0, position.x, position.y, position.z, target.x, target.y, target.z, up.x, up.y, up.z)
            return Matrix4f(arr)
        }

    var yaw: Float = 0f
        set(value) {
            field = value
            update()
        }

    var pitch: Float = 0f
        set(value) {
            field = min(max(value, 89f), -89f)
            update()
        }

    init {
        update()
    }

    private fun update() {
        front = Float3().also {
            it.x = cos(yaw) * cos(pitch)
            it.y = sin(pitch)
            it.z = sin(yaw) * cos(pitch)
            it.normalize()
        }

        right = front.cross(worldUp)
        right.normalize()

        up = right.cross(worldUp)
        up.normalize()
    }
}

fun Float3.normalize() {
    val magnitude = sqrt(x * x + y * y + z * z)
    x /= magnitude
    y /= magnitude
    z /= magnitude
}

fun Float3.cross(other: Float3) = Float3(
    y * other.z - other.y * z,
    z * other.x - other.z * x,
    x * other.y - other.x * y
)

fun Float3.add(other: Float3) = Float3(x + other.x, y + other.y, z + other.z)

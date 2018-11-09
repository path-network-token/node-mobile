package network.path.mobilenode.ui.opengl.glutils

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.CharBuffer
import java.nio.FloatBuffer
import java.nio.ShortBuffer

fun createBuffer(arr: FloatArray): FloatBuffer = ByteBuffer.allocateDirect(arr.size * 4).run {
    // use the device hardware's native byte order
    order(ByteOrder.nativeOrder())

    // create a floating point buffer from the ByteBuffer
    asFloatBuffer().apply {
        // add the coordinates to the FloatBuffer
        put(arr)
        // set the buffer to read the first coordinate
        position(0)
    }
}

fun createBuffer(arr: ByteArray): ByteBuffer = ByteBuffer.allocateDirect(arr.size * 4).also {
    // create a floating point buffer from the ByteBuffer
    // add the coordinates to the FloatBuffer
    it.put(arr)
    // set the buffer to read the first coordinate
    it.position(0)
}

fun createBuffer(arr: CharArray): CharBuffer = ByteBuffer.allocateDirect(arr.size * 4).run {
    // use the device hardware's native byte order
    order(ByteOrder.nativeOrder())

    // create a floating point buffer from the ByteBuffer
    asCharBuffer().apply {
        // add the coordinates to the FloatBuffer
        put(arr)
        // set the buffer to read the first coordinate
        position(0)
    }
}

fun createBuffer(arr: ShortArray): ShortBuffer = ByteBuffer.allocateDirect(arr.size * 4).run {
    // use the device hardware's native byte order
    order(ByteOrder.nativeOrder())

    // create a floating point buffer from the ByteBuffer
    asShortBuffer().apply {
        // add the coordinates to the FloatBuffer
        put(arr)
        // set the buffer to read the first coordinate
        position(0)
    }
}

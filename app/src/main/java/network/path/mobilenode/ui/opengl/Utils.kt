package network.path.mobilenode.ui.opengl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.opengl.GLES20
import android.opengl.GLUtils
import androidx.annotation.ColorInt
import androidx.annotation.RawRes

fun CharArray.addMultiple(index: Int, vararg v: Int): Int {
    v.forEachIndexed { offset, i -> this[index + offset] = i.toChar() }
    return index + v.size
}

fun ShortArray.addMultiple(index: Int, vararg v: Short): Int {
    v.forEachIndexed { offset, i -> this[index + offset] = i }
    return index + v.size
}

fun FloatArray.addMultiple(index: Int, vararg v: Float): Int {
    v.forEachIndexed { offset, i -> this[index + offset] = i }
    return index + v.size
}

fun loadAsset(context: Context, assetPath: String): String =
        context.assets.open(assetPath).bufferedReader().use {
            it.readText()
        }

fun loadRawResource(context: Context, @RawRes resId: Int): String =
        context.resources.openRawResource(resId).bufferedReader().use {
            it.readText()
        }

fun colorToFloatArray(@ColorInt color: Int) =
        floatArrayOf(Color.red(color).toFloat() / 255f,
                Color.green(color).toFloat() / 255f,
                Color.blue(color).toFloat() / 255f,
                Color.alpha(color).toFloat() / 255f)

private fun Bitmap.flip(isVertical: Boolean): Bitmap {
    val matrix = Matrix().apply {
        if (isVertical) {
            postScale(1f, -1f)
        } else {
            postScale(-1f, 1f)
        }
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun loadTexture(context: Context, resourceId: Int, flipVertical: Boolean = false): Int {
    val textureHandle = IntArray(1)

    GLES20.glGenTextures(1, textureHandle, 0)

    if (textureHandle[0] == 0) {
        throw RuntimeException("Error loading texture.")
    }

    val options = BitmapFactory.Options()
    options.inScaled = false   // No pre-scaling
    options.inPreferredConfig = Bitmap.Config.ARGB_8888

    // Read in the resource
    val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
    val finalBitmap = if (flipVertical) bitmap.flip(true) else bitmap

    // Bind to the texture in OpenGL
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

    // Set filtering
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)

    // Load the bitmap into the bound texture.
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, finalBitmap, 0)

    // Recycle the bitmap, since its data has been loaded into OpenGL.
    bitmap.recycle()

    return textureHandle[0]
}

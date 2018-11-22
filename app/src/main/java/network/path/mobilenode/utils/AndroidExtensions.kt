package network.path.mobilenode.utils

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import timber.log.Timber

fun EditText.onTextChanged(callback: (CharSequence) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
            callback(text)
        }

        override fun afterTextChanged(editable: Editable?) = Unit

        override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) = Unit
    })
}

fun showToast(context: Context, @StringRes messageResId: Int) {
    Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()
}

/**
 * Wrapper for kotlin.concurrent.thread that tracks uncaught exceptions.
 */
fun thread(name: String? = null, start: Boolean = true, isDaemon: Boolean = false,
           contextClassLoader: ClassLoader? = null, priority: Int = -1, block: () -> Unit): Thread {
    val thread = kotlin.concurrent.thread(false, isDaemon, contextClassLoader, name, priority, block)
    thread.setUncaughtExceptionHandler { _, t -> Timber.e(t) }
    if (start) thread.start()
    return thread
}

fun Context.launchUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    startActivity(intent)
}

fun View.animateScale(toScale: Float, duration: Long = 250L) {
    val animator = ValueAnimator.ofFloat(1f, toScale, 1f)
    animator.duration = duration
    animator.addUpdateListener {
        val value = it.animatedValue as Float
        this.scaleY = value
        this.scaleX = value
    }
    animator.start()
}

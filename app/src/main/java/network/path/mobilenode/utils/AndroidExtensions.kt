package network.path.mobilenode.utils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
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

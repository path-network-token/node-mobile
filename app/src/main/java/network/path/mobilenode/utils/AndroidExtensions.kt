package network.path.mobilenode.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.CharacterStyle
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.EditText
import android.widget.TextView
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

fun String.toHtml(): Spanned = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
    Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
} else {
    @Suppress("DEPRECATION")
    Html.fromHtml(this)
}

fun TextView.setTextWithLinks(text: String, linkCallback: ((String) -> Unit)? = null) {
    val html = text.toHtml()
    if (linkCallback != null) {
        val newText = SpannableUtils.replaceAll(html, URLSpan::class.java, CustomClickableSpan.CONVERTER, linkCallback)
        this.setText(newText, TextView.BufferType.SPANNABLE)
        this.movementMethod = LinkMovementMethod.getInstance()
    } else {
        this.text = html
    }
}

object SpannableUtils {
    fun <A : CharacterStyle, B : CharacterStyle> replaceAll(original: Spanned,
                                                            sourceType: Class<A>,
                                                            converter: SpanConverter<A, B>,
                                                            callback: (String) -> Unit): Spannable {

        val result = SpannableString(original)
        val spans = result.getSpans(0, result.length, sourceType)

        for (span in spans) {
            val start = result.getSpanStart(span)
            val end = result.getSpanEnd(span)
            val flags = result.getSpanFlags(span)

            result.removeSpan(span)
            result.setSpan(converter.convert(span, callback), start, end, flags)
        }

        return result
    }

    interface SpanConverter<A : CharacterStyle, B : CharacterStyle> {
        fun convert(span: A, callback: (String) -> Unit): B
    }
}

class CustomClickableSpan(private val url: String,
                          private val callback: (String) -> Unit) : ClickableSpan() {
    companion object {
        val CONVERTER = object : SpannableUtils.SpanConverter<URLSpan, CustomClickableSpan> {
            override fun convert(span: URLSpan, callback: (String) -> Unit) =
                    CustomClickableSpan(span.url, callback)
        }
    }

    override fun onClick(widget: View) {
        callback(url)
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = false
    }
}

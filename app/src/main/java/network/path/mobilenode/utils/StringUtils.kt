package network.path.mobilenode.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.CharacterStyle
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import network.path.mobilenode.R
import java.text.DecimalFormat
import java.util.*



val SIGN_FORMAT = DecimalFormat("+#,###,###;-#,###,###")

fun Context.formatDifference(
        newValue: Long,
        oldValue: Long?,
        @ColorRes positiveColor: Int = R.color.apple_green,
        @ColorRes negativeColor: Int = R.color.coral_pink,
        separator: String = " ",
        numberFormat: String = "%,d",
        stringFormat: String = "%s",
        locale: Locale = Locale.getDefault()): SpannableStringBuilder {
    val sb = SpannableStringBuilder(stringFormat.format(locale, numberFormat.format(locale, newValue)))
    if (oldValue != null && oldValue != newValue) {
        val color = ContextCompat.getColor(this, if (oldValue > newValue) negativeColor else positiveColor)
        val formattedValue = SpannableString(stringFormat.format(locale, SIGN_FORMAT.format(newValue - oldValue)))
        formattedValue.setSpan(ForegroundColorSpan(color), 0, formattedValue.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        sb.append(separator).append(formattedValue)
    }
    return sb
}

fun String.toHtml(): Spanned = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
    Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
} else {
    @Suppress("DEPRECATION")
    (Html.fromHtml(this))
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

class TypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {
    override fun updateMeasureState(p: TextPaint) {
        p.typeface = typeface
        p.flags = p.flags or Paint.SUBPIXEL_TEXT_FLAG
    }

    override fun updateDrawState(tp: TextPaint) {
        tp.typeface = typeface
        tp.flags = tp.flags or Paint.SUBPIXEL_TEXT_FLAG
    }
}

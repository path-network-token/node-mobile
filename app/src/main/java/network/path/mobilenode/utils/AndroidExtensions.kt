package network.path.mobilenode.utils

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.FontRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.textfield.TextInputLayout
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

fun Context.launchUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    startActivity(intent)
}

fun View.bounceScale(toScale: Float, duration: Long = 250L) {
    val animator = ValueAnimator.ofFloat(1f, toScale, 1f)
    animator.duration = duration
    animator.addUpdateListener {
        val value = it.animatedValue as Float
        this.scaleY = value
        this.scaleX = value
    }
    animator.start()
}

fun EditText.toggleSoftKeyboard(visible: Boolean) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (visible) {
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    } else {
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}

fun Resources.dpToPx(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics)

fun TextInputLayout.setError(error: CharSequence?, @FontRes fontId: Int) {
    this.error = if (error != null) {
        val s = SpannableString(error)
        val typeface = ResourcesCompat.getFont(context, fontId)
        if (typeface != null) {
            s.setSpan(TypefaceSpan(typeface), 0, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        s
    } else null
}

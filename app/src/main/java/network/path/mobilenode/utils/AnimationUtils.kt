package network.path.mobilenode.utils

import android.animation.Animator
import android.content.Context
import android.util.Property
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextSwitcher
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.annotation.FontRes
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.animation.doOnEnd
import androidx.core.content.res.ResourcesCompat

class TranslationFractionProperty(private val vertical: Boolean) : Property<View, Float>(Float::class.java, "translateFraction") {
    override fun get(view: View): Float {
        val parentValue = getParentValue(view)
        val viewValue = if (vertical) view.translationY else view.translationX
        return if (parentValue <= 0) viewValue else viewValue / parentValue
    }

    override fun set(view: View, value: Float) {
        val parentValue = getParentValue(view)
        if (parentValue > 0) {
            val newValue = parentValue * value
            if (vertical) {
                view.translationY = newValue
            } else {
                view.translationX = newValue
            }
        }
    }

    private fun getParentValue(view: View): Int {
        val parent = view.parent as? ViewGroup
        return (if (vertical) parent?.measuredHeight else parent?.measuredWidth) ?: 0
    }
}

fun Animator.startAfter(animator: Animator?) {
    if (animator != null) {
        animator.doOnEnd { this.start() }
    } else {
        this.start()
    }
}

fun Context.setupFadeTextSwitchers(@FontRes fontRes: Int, @StyleRes themeRes: Int, extraSetup: ((TextView) -> Unit)?, vararg switchers: TextSwitcher) {
    val factory = ViewSwitcher.ViewFactory {
        val typeface = ResourcesCompat.getFont(this, fontRes)
        val view = TextView(ContextThemeWrapper(this, themeRes), null, 0)
        view.typeface = typeface
        if (extraSetup != null) {
            extraSetup(view)
        }
        view
    }

    val inAnim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in).apply { duration = 200 }
    val outAnim = AnimationUtils.loadAnimation(this, android.R.anim.fade_out).apply { duration = 200 }
    fun setup(switcher: TextSwitcher) {
        switcher.setFactory(factory)
        switcher.inAnimation = inAnim
        switcher.outAnimation = outAnim
    }

    switchers.forEach(::setup)
}

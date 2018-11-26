package network.path.mobilenode.utils

import android.animation.Animator
import android.util.Property
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd

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

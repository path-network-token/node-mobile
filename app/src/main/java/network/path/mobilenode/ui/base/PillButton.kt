package network.path.mobilenode.ui.base

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import network.path.mobilenode.R


class PillButton : Button {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val animatedDrawable = ContextCompat.getDrawable(context, R.drawable.pill_button_ripple) as? AnimatedVectorDrawable
    private val otherDrawable = ContextCompat.getDrawable(context, R.drawable.pill_button_active)

    private val runnable = Runnable {
        background = animatedDrawable
        animatedDrawable?.start()
    }

    init {
        typeface = ResourcesCompat.getFont(context, R.font.exo_bold)
        updateDrawable()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        updateDrawable()
    }

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        updateDrawable()
    }

    fun updateDrawable() {
        if (!isEnabled || isPressed) {
            handler?.removeCallbacks(runnable)
            animatedDrawable?.stop()
            background = otherDrawable
        } else {
            if (handler != null) {
                handler.postDelayed(runnable, 250)
            } else {
                runnable.run()
            }
        }
    }
}

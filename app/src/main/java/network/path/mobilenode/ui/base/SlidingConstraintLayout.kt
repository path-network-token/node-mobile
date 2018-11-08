package network.path.mobilenode.ui.base

import android.content.Context
import android.util.AttributeSet
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * Created by paveld on 4/13/14.
 */
class SlidingConstraintLayout
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : ConstraintLayout(context, attrs, defStyle) {

    private var preDrawListener: ViewTreeObserver.OnPreDrawListener? = null

    var yFraction = 0f
        set(fraction) {
            field = fraction

            if (height == 0) {
                if (preDrawListener == null) {
                    preDrawListener = ViewTreeObserver.OnPreDrawListener {
                        viewTreeObserver.removeOnPreDrawListener(preDrawListener)
                        yFraction = yFraction
                        true
                    }
                    viewTreeObserver.addOnPreDrawListener(preDrawListener)
                }
                return
            }

            val translationY = height * fraction
            setTranslationY(translationY)
        }
}

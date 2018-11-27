package network.path.mobilenode.ui.main.jobreport

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.res.use
import kotlinx.android.synthetic.main.latency_chart.view.*
import network.path.mobilenode.R
import network.path.mobilenode.utils.formatDifference
import network.path.mobilenode.utils.startAfter

class LatencyChart
@SuppressLint("Recycle") //obtainStyledAttributes recycled in ktx
@JvmOverloads
constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    var animator: Animator? = null
        private set

    init {
        inflate(context, R.layout.latency_chart, this)

        latencyValueTextView.alpha = 0f
        context.obtainStyledAttributes(attrs, R.styleable.LatencyChart, defStyleAttr, defStyleRes).use {
            val titleText = it.getString(R.styleable.LatencyChart_label)
            val progressMillis = it.getInt(R.styleable.LatencyChart_progress_millis, 0)
            val maxMillis = it.getInt(R.styleable.LatencyChart_max_millis, 0)

            latencyChartLabelTextView.text = titleText
            latencyValueTextView.text = context.getString(R.string.latency_chart_value, progressMillis.toString())

            latencyProgressBar.progress = progressMillis
            latencyProgressBar.max = maxMillis
        }
    }

    fun setLatencyMillis(latency: Long, firstLatency: Long?, maxMillis: Long, after: Animator? = null) {
        latencyProgressBar.max = maxMillis.toInt()

        animator?.cancel()
        animator = null

        if (latencyValueTextView.alpha < 1f || latencyProgressBar.progress != latency.toInt()) {
            val progressAnimator = ObjectAnimator.ofInt(latencyProgressBar, "progress", latencyProgressBar.progress, latency.toInt())
            progressAnimator.duration = 250L
            val alphaAnimator = ObjectAnimator.ofFloat(latencyValueTextView, "alpha", latencyValueTextView.alpha, 1f)
            alphaAnimator.duration = 150L

            val set = AnimatorSet()
            set.playSequentially(progressAnimator, alphaAnimator)
            set.startAfter(after)
            set.doOnEnd { animator = null }
            animator = set
        }

        val formattedValue = context.formatDifference(latency,
                firstLatency,
                R.color.coral_pink,
                R.color.apple_green,
                "\n",
                context.getString(R.string.latency_chart_value))
        latencyValueTextView.setText(formattedValue, TextView.BufferType.SPANNABLE)
    }

    fun setLabel(label: CharSequence) {
        latencyChartLabelTextView.setText(label, TextView.BufferType.SPANNABLE)
    }
}

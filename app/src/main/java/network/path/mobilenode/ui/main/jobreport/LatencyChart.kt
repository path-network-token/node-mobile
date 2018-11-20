package network.path.mobilenode.ui.main.jobreport

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import kotlinx.android.synthetic.main.latency_chart.view.*
import network.path.mobilenode.R
import network.path.mobilenode.utils.formatDifference

class LatencyChart
@SuppressLint("Recycle") //obtainStyledAttributes recycled in ktx
@JvmOverloads
constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var animator: ValueAnimator? = null

    init {
        inflate(context, R.layout.latency_chart, this)

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

    fun setLatencyMillis(latency: Long, firstLatency: Long?, maxMillis: Long) {
        latencyProgressBar.max = maxMillis.toInt()

        val animator = ValueAnimator.ofInt(latencyProgressBar.progress, latency.toInt())
        animator.duration = 250L
        animator.addUpdateListener {
            latencyProgressBar.progress = it.animatedValue as Int
        }
        animator.start()
        this.animator?.cancel()
        this.animator = animator

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

package network.path.mobilenode.ui.main.jobreport

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.core.animation.doOnEnd
import kotlinx.android.synthetic.main.average_latency_layout.*
import kotlinx.android.synthetic.main.fragment_job_report.*
import kotlinx.android.synthetic.main.job_types_layout.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import network.path.mobilenode.R
import network.path.mobilenode.domain.entity.CheckType
import network.path.mobilenode.domain.entity.CheckTypeStatistics
import network.path.mobilenode.ui.base.BaseFragment
import network.path.mobilenode.utils.TranslationFractionProperty
import network.path.mobilenode.utils.bounceScale
import network.path.mobilenode.utils.formatDifference
import network.path.mobilenode.utils.observe
import network.path.mobilenode.utils.startAfter
import org.koin.androidx.viewmodel.ext.android.viewModel

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class JobReportFragment : BaseFragment() {
    companion object {
        private const val CHECKS_COUNT = 3
        private const val ANIMATION_DURATION = 500L
    }

    override val layoutResId = R.layout.fragment_job_report

    private val jobReportViewModel by viewModel<JobReportViewModel>()
    private var progressAnimator: ObjectAnimator? = null
    private var enterAnimator: ObjectAnimator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        closeScreenImageView.setOnClickListener {
            requireActivity().onBackPressed()
        }

        jobTypesButtonsPanel.setOnCheckedChangeListener { _, checkedId ->
            selectStatistics(checkedId)
        }

        jobReportViewModel.let {
            it.onViewCreated()
            it.statistics.observe(this, ::setStatistics)
            it.selectedType.observe(this, ::setSelected)
        }

        animateIn()
    }

    private fun animateIn() {
        jobPercentageProgressBar.translationX = 1000f

        val pctAnimator = ObjectAnimator.ofFloat(jobPercentageProgressBar, TranslationFractionProperty(false), 0.5f, 0f)
        pctAnimator.duration = 250L
        pctAnimator.interpolator = AccelerateDecelerateInterpolator()

        pctAnimator.startDelay = 250L
        pctAnimator.start()
        pctAnimator.doOnEnd { enterAnimator = null }
        enterAnimator = pctAnimator
    }

    private fun selectStatistics(@IdRes id: Int) {
        val stats = jobReportViewModel.statistics.value
        if (stats != null) {
            val value = when (id) {
                R.id.checksButton1 -> stats[0]
                R.id.checksButton2 -> stats[1]
                R.id.checksButtonOther -> stats[CHECKS_COUNT - 1]
                else -> stats[CHECKS_COUNT - 1]
            }
            jobReportViewModel.select(value.type)
        }
    }

    private fun setSelected(checkType: CheckType?) {
        val stats = jobReportViewModel.statistics.value ?: return

        val value = stats.find { it.type == checkType } ?: stats.last()
        val total = stats.sumBy { it.count.toInt() }
        val pct = if (total == 0) 0f else (value.count * 100f / total)
        jobPercentageTextView.text = getString(R.string.job_percentage, pct)

        setPercent(pct)
    }

    private fun setPercent(pct: Float) {
        val oldAnimator = progressAnimator
        val fraction = if (oldAnimator != null) {
            oldAnimator.cancel()
            oldAnimator.animatedFraction
        } else 1f

        val view = jobPercentageProgressBar ?: return
        val animator = ObjectAnimator.ofInt(view, "progress", view.progress, pct.toInt())
        animator.duration = (ANIMATION_DURATION * fraction).toLong()
        animator.startAfter(enterAnimator)
        progressAnimator = animator
    }

    private fun setStatistics(statistics: List<CheckTypeStatistics>) {
        val maxMillis = statistics.map { it.averageLatency }.max() ?: 10_000L

        val first = statistics[0]
        set(checksButton1, latencyChart1, first, maxMillis, enterAnimator)

        val second = statistics[1]
        set(checksButton2, latencyChart2, second, maxMillis, latencyChart1.animator)

        val other = statistics[CHECKS_COUNT - 1]
        set(checksButtonOther, latencyChartOther, other, maxMillis, latencyChart2.animator)

        // Total jobs count
        val firstValue = jobReportViewModel.firstStats?.map { it.count }?.sum()
        val totalValue = statistics.map { it.count }.sum()
        val formattedValue = requireContext().formatDifference(totalValue, firstValue)
        totalJobsLabel.setText(TextUtils.concat(getString(R.string.total_jobs_performed).toUpperCase(), " ", formattedValue), TextView.BufferType.SPANNABLE)

        if (firstValue != null && firstValue != totalValue) {
            totalJobsLabel.bounceScale(1.1f)
        }
    }

    private fun set(button: RadioButton, chart: LatencyChart, stat: CheckTypeStatistics, maxMillis: Long, after: Animator? = null) {
        val title = stat.type.title
        button.text = title

        // Calculate difference
        val firstValue = jobReportViewModel.firstStats?.find { it.type == stat.type }
        val formattedValue = requireContext().formatDifference(
                stat.count,
                firstValue?.count
        )
        chart.setLabel(TextUtils.concat("$title (", formattedValue, ")"))
        chart.setLatencyMillis(stat.averageLatency, firstValue?.averageLatency, maxMillis, after)
        if (firstValue != null && stat.count != firstValue.count) {
            chart.bounceScale(1.1f)
        }
    }

    private val CheckType?.title
        get() = getString(when (this) {
            null -> R.string.other_checks
            CheckType.HTTP -> R.string.http_checks
            CheckType.TCP -> R.string.tcp_checks
            CheckType.UDP -> R.string.udp_checks
            CheckType.DNS -> R.string.dns_checks
            CheckType.TRACEROUTE -> R.string.traceroute_checks
            CheckType.UNKNOWN -> R.string.unknown_checks
        }).toUpperCase()
}

package network.path.mobilenode.ui.main.jobreport

import android.animation.ValueAnimator
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.IdRes
import kotlinx.android.synthetic.main.average_latency_layout.*
import kotlinx.android.synthetic.main.fragment_job_report.*
import kotlinx.android.synthetic.main.job_types_layout.*
import network.path.mobilenode.R
import network.path.mobilenode.domain.entity.CheckType
import network.path.mobilenode.domain.entity.CheckTypeStatistics
import network.path.mobilenode.ui.base.BaseFragment
import network.path.mobilenode.utils.animateScale
import network.path.mobilenode.utils.formatDifference
import network.path.mobilenode.utils.observe
import org.koin.androidx.viewmodel.ext.android.viewModel

class JobReportFragment : BaseFragment() {
    companion object {
        private const val CHECKS_COUNT = 3
        private const val ANIMATION_DURATION = 500L
    }

    override val layoutResId = R.layout.fragment_job_report

    private val jobReportViewModel by viewModel<JobReportViewModel>()
    private var progressAnimator: ValueAnimator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCloseScreenButton()
        setupJobTypesClickListeners()

        jobReportViewModel.let {
            it.onViewCreated()
            it.statistics.observe(this, ::setStatistics)
            it.selectedType.observe(this, ::setSelected)
        }
    }

    private fun setupCloseScreenButton() {
        closeScreenImageView.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupJobTypesClickListeners() {
        jobTypesButtonsPanel.setOnCheckedChangeListener { _, checkedId ->
            selectStatistics(checkedId)
        }
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
        val pct = value.count * 100f / total
        jobPercentageTextView.text = getString(R.string.job_percentage, pct)

        setPercent(pct)
    }

    private fun setPercent(pct: Float) {
        val oldAnimator = progressAnimator
        val fraction = if (oldAnimator != null) {
            oldAnimator.cancel()
            oldAnimator.animatedFraction
        } else 1f

        val view = jobPercentageProgressBar
        val animator = ValueAnimator.ofInt(view?.progress ?: 0, pct.toInt())
        animator.duration = (ANIMATION_DURATION * fraction).toLong()
        animator.addUpdateListener {
            view?.progress = it.animatedValue as Int
        }
        animator.start()
        progressAnimator = animator
    }

    private fun setStatistics(statistics: List<CheckTypeStatistics>) {
        val maxMillis = statistics.map { it.averageLatency }.max() ?: 10_000L

        val first = statistics[0]
        set(checksButton1, latencyChart1, first, maxMillis)

        val second = statistics[1]
        set(checksButton2, latencyChart2, second, maxMillis)

        val other = statistics[CHECKS_COUNT - 1]
        set(checksButtonOther, latencyChartOther, other, maxMillis)

        // Total jobs count
        val firstValue = jobReportViewModel.firstStats?.map { it.count }?.sum()
        val totalValue = statistics.map { it.count }.sum()
        val formattedValue = requireContext().formatDifference(totalValue, firstValue)
        totalJobsLabel.setText(TextUtils.concat(getString(R.string.total_jobs_performed).toUpperCase(), " ", formattedValue), TextView.BufferType.SPANNABLE)

        if (firstValue != null && firstValue != totalValue) {
            totalJobsLabel.animateScale(1.1f)
        }
    }

    private fun set(button: RadioButton, chart: LatencyChart, stat: CheckTypeStatistics, maxMillis: Long) {
        val title = stat.type.title
        button.text = title

        // Calculate difference
        val firstValue = jobReportViewModel.firstStats?.find { it.type == stat.type }
        val formattedValue = requireContext().formatDifference(
                stat.count,
                firstValue?.count
        )
        chart.setLabel(TextUtils.concat("$title (", formattedValue, ")"))
        chart.setLatencyMillis(stat.averageLatency, firstValue?.averageLatency, maxMillis)

        if (firstValue != null && stat.count != firstValue.count) {
            chart.animateScale(1.1f)
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

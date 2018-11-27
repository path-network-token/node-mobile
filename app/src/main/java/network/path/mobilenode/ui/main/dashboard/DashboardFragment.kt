package network.path.mobilenode.ui.main.dashboard

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.dashboard_details.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.job_report_button.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import network.path.mobilenode.R
import network.path.mobilenode.domain.entity.AutonomousSystem
import network.path.mobilenode.domain.entity.ConnectionStatus
import network.path.mobilenode.domain.entity.JobList
import network.path.mobilenode.ui.base.BaseFragment
import network.path.mobilenode.ui.opengl.OpenGLSurfaceView
import network.path.mobilenode.utils.TranslationFractionProperty
import network.path.mobilenode.utils.observe
import network.path.mobilenode.utils.setupFadeTextSwitchers
import org.koin.androidx.viewmodel.ext.android.viewModel

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class DashboardFragment : BaseFragment() {
    companion object {
        private const val STATE_OPENGL = "STATE_OPENGL"

        fun newInstance() = DashboardFragment()
    }

    override val layoutResId = R.layout.fragment_dashboard

    private val dashboardViewModel by viewModel<DashboardViewModel>()

    private lateinit var openGlSurfaceView: OpenGLSurfaceView
    private var openGlState: Bundle? = null

    private var runningAnimator: ValueAnimator? = null

    private var previousStatus: ConnectionStatus? = null
    private var statusAnimator: Animator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClicks()
        setupTexts()

        dashboardViewModel.let {
            it.onViewCreated()
            it.nodeId.observe(this, ::setNodeId)
            it.status.observe(this, ::setStatus)
            it.operatorDetails.observe(this, ::setOperatorDetails)
            it.ipAddress.observe(this, ::setIpAddress)
            it.jobList.observe(this, ::setJobList)
            it.isRunning.observe(this, ::setRunning)
        }

        openGlSurfaceView = surfaceView
        restoreGlState(savedInstanceState)

        animateIn()
    }

    override fun onPause() {
        super.onPause()
        openGlSurfaceView.onPause()
    }

    override fun onResume() {
        super.onResume()
        openGlSurfaceView.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        saveGlState()
        openGlSurfaceView.destroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveGlState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        restoreGlState(savedInstanceState)
    }

    // STATE MANAGEMENT
    private fun saveGlState(outState: Bundle? = null) {
        openGlState = openGlSurfaceView.saveState()
        outState?.putBundle(STATE_OPENGL, openGlState)
    }

    private fun restoreGlState(savedInstanceState: Bundle? = null) {
        val state = savedInstanceState?.getBundle(STATE_OPENGL) ?: openGlState
        if (state != null) {
            openGlSurfaceView.restoreState(state)
        }
    }

    // Private
    private fun animateIn() {
        separator.translationX = -1000f
        updateAlpha(0f)
        jobReportButton.translationY = 1000f

        val dividerAnimator = ObjectAnimator.ofFloat(separator, TranslationFractionProperty(false), -0.6f, 0f)
        dividerAnimator.duration = 300L
        dividerAnimator.interpolator = AccelerateDecelerateInterpolator()

        val alphaAnimator = ValueAnimator.ofFloat(0f, 1f)
        alphaAnimator.addUpdateListener {
            val progress = it.animatedValue as Float
            updateAlpha(progress)
        }
        alphaAnimator.duration = 250L
        alphaAnimator.startDelay = 200L
        alphaAnimator.interpolator = LinearInterpolator()

        val buttonAnimator = ObjectAnimator.ofFloat(jobReportButton, TranslationFractionProperty(true), 0.3f, 0f)
        buttonAnimator.duration = 250L
        buttonAnimator.interpolator = DecelerateInterpolator()

        val set = AnimatorSet()
        set.play(dividerAnimator).with(alphaAnimator).before(buttonAnimator)
        set.start()
    }

    private fun updateAlpha(alpha: Float) {
        statusDot?.alpha = alpha
        labelStatus?.alpha = alpha
        subnetAddressLabel?.alpha = alpha
        ipWithSubnetAddress?.alpha = alpha
        nodeIdTextView?.alpha = alpha
        dashboardDetails?.alpha = alpha
    }

    private fun setupClicks() {
        viewJobReportButton.setOnClickListener {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_mainFragment_to_jobReportFragment)
        }

        toggleButton.setOnClickListener {
            dashboardViewModel.toggle()
        }

        infoButton.setOnClickListener {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_mainFragment_to_aboutFragment)
        }
    }

    private fun setupTexts() {
        val context = requireContext()
        context.setupFadeTextSwitchers(R.font.exo_regular, R.style.DashboardDetails, null, value1, value2, value3, value4)
        context.setupFadeTextSwitchers(R.font.exo_bold, R.style.LabelStatus, {
            it.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }, labelStatus)
    }

    private fun setNodeId(nodeId: String?) {
        nodeIdTextView.text = getString(R.string.node_id, nodeId
                ?: getString(R.string.unconfirmed_node_id))
    }

    private fun setIpAddress(ipAddress: String?) {
        ipWithSubnetAddress.text = ipAddress ?: getString(R.string.n_a)
    }

    private fun setStatus(status: ConnectionStatus) {
        labelStatus.setText(status.label)
        openGlSurfaceView.setConnectionStatus(status)

        val oldStatus = previousStatus
        if (oldStatus != null && oldStatus != status) {
            statusAnimator?.cancel()

            val animator = ValueAnimator.ofArgb(oldStatus.dotColor, status.dotColor)
            animator.addUpdateListener {
                val progress = it.animatedValue as Int
                ImageViewCompat.setImageTintList(statusDot, ColorStateList.valueOf(progress))
            }

            val textAnimator = ValueAnimator.ofArgb(oldStatus.textColor, status.textColor)
            textAnimator.addUpdateListener {
                val progress = it.animatedValue as Int
                subnetAddressLabel?.setTextColor(progress)
            }

            val separatorAnimator = ValueAnimator.ofArgb(oldStatus.separatorColor, status.separatorColor)
            separatorAnimator.addUpdateListener {
                val progress = it.animatedValue as Int
                separator?.setBackgroundColor(progress)
            }

            val set = AnimatorSet()
            set.playTogether(animator, textAnimator, separatorAnimator)
            set.duration = 400L
            set.start()

            statusAnimator = set
        } else {
            ImageViewCompat.setImageTintList(statusDot, ColorStateList.valueOf(status.dotColor))
            subnetAddressLabel.setTextColor(status.textColor)
            separator.setBackgroundColor(status.separatorColor)
        }
        previousStatus = status
    }

    private fun setOperatorDetails(details: AutonomousSystem?) {
        value1.setText(details?.asNumber.orNoData())
        value2.setText(details?.asDescription.orNoData())
        value3.setText(details?.asCountryCode.orNoData())
        value4.setText(getString(R.string.android))
    }

    private fun setJobList(jobList: JobList) {
        value1.setText(jobList.asn?.orNoData())
        ipWithSubnetAddress.text = jobList.networkPrefix ?: getString(R.string.n_a)
    }

    private fun setRunning(isRunning: Boolean) {
        if (isRunning == toggleButton.isSelected) {
            toggleButton.isSelected = !isRunning

            val oldAnimator = runningAnimator
            val fraction = if (oldAnimator != null) {
                oldAnimator.cancel()
                oldAnimator.animatedFraction
            } else 1f
            runningAnimator = null

            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.addUpdateListener {
                val progress = it.animatedValue as Float
                diagonalLine?.alpha = progress
                pausedBackground?.alpha = progress * 0.9f
                labelPaused?.alpha = progress
            }
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.duration = (250 * fraction).toLong()
            if (isRunning) {
                animator.doOnEnd { updatePauseVisibility(true) }
                animator.reverse()
            } else {
                animator.doOnStart { updatePauseVisibility(false) }
                animator.start()
            }

            openGlSurfaceView.setRunning(isRunning)
        }
    }

    private fun updatePauseVisibility(hidden: Boolean) {
        diagonalLine?.visibility = if (hidden) View.GONE else View.VISIBLE
        labelPaused?.visibility = if (hidden) View.GONE else View.VISIBLE
        pausedBackground?.visibility = if (hidden) View.GONE else View.VISIBLE
    }

    private fun String?.orNoData() = this ?: getString(R.string.no_data)

    private val ConnectionStatus.label: String
        get() = getString(when (this) {
            ConnectionStatus.CONNECTED -> R.string.status_connected
            ConnectionStatus.PROXY -> R.string.status_proxy
            ConnectionStatus.DISCONNECTED -> R.string.status_disconnected
        })

    private val ConnectionStatus.dotColor: Int
        get() = ContextCompat.getColor(requireContext(), when (this) {
            ConnectionStatus.CONNECTED -> R.color.apple_green
            ConnectionStatus.PROXY -> R.color.amber
            ConnectionStatus.DISCONNECTED -> R.color.coral_pink
        })

    private val ConnectionStatus.textColor: Int
        get() = ContextCompat.getColor(requireContext(), when (this) {
            ConnectionStatus.CONNECTED, ConnectionStatus.PROXY -> R.color.light_teal
            ConnectionStatus.DISCONNECTED -> R.color.coral_pink
        })

    private val ConnectionStatus.separatorColor: Int
        get() = ContextCompat.getColor(requireContext(), when (this) {
            ConnectionStatus.CONNECTED, ConnectionStatus.PROXY -> R.color.tealish
            ConnectionStatus.DISCONNECTED -> R.color.coral_pink
        })
}

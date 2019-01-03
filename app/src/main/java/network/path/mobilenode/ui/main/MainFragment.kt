package network.path.mobilenode.ui.main

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import androidx.fragment.app.transaction
import kotlinx.android.synthetic.main.fragment_main.*
import network.path.mobilenode.R
import network.path.mobilenode.ui.base.BaseFragment
import network.path.mobilenode.ui.main.dashboard.DashboardFragment
import network.path.mobilenode.ui.main.wallet.WalletFragment
import network.path.mobilenode.utils.observe
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainFragment : BaseFragment() {
    override val layoutResId = R.layout.fragment_main

    private val walletFragment by lazy { WalletFragment.newInstance() }
    private val dashboardFragment by lazy { DashboardFragment.newInstance() }

    private val viewModel by viewModel<MainViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            dashboardRadioButton.isChecked = true
            showDashboardFragment()
        }

        initControls()

        viewModel.let {
            it.onViewCreated()
            if (it.isLooking.value == false) {
                locatingContainer.visibility = View.GONE
            } else {
                fragmentContainer.alpha = 0f
                bottomBarLayout.alpha = 0f
                it.isLooking.observe(this, ::setLooking)
            }
        }
    }

    private fun initControls() {
        walletRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                childFragmentManager.transaction {
                    replace(R.id.fragmentContainer, walletFragment)
                }
            }
        }
        dashboardRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showDashboardFragment()
            }
        }

        locatingText.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.blink)
    }

    private fun showDashboardFragment() {
        childFragmentManager.transaction {
            replace(R.id.fragmentContainer, dashboardFragment)
        }
    }

    private fun setLooking(isLooking: Boolean) {
        if (!isLooking) {
            Handler().postDelayed({
                val alphaAnimator = ObjectAnimator.ofFloat(fragmentContainer, "alpha", 0f, 1f)
                val panelAnimator = ObjectAnimator.ofFloat(bottomBarLayout, "alpha", 0f, 1f)
                val locatingAnimator = ObjectAnimator.ofFloat(locatingContainer, "alpha", 1f, 0f)
                val scaleXAnimator = ObjectAnimator.ofFloat(locatingContainer, "scaleX", 1f, 2f)
                val scaleYAnimator = ObjectAnimator.ofFloat(locatingContainer, "scaleY", 1f, 2f)
                val set = AnimatorSet()
                set.duration = 250
                set.interpolator = AccelerateDecelerateInterpolator()
                set.playTogether(alphaAnimator, panelAnimator, locatingAnimator, scaleXAnimator, scaleYAnimator)
                set.start()
            }, 1000)
        }
    }
}

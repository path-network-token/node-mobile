package network.path.mobilenode.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.transaction
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.fragment_main.*
import network.path.mobilenode.R
import network.path.mobilenode.ui.base.BaseFragment
import network.path.mobilenode.ui.main.dashboard.DashboardFragment
import network.path.mobilenode.ui.main.wallet.WalletFragment
import network.path.mobilenode.ui.opengl.OpenGLSurfaceView

class MainFragment : BaseFragment() {
    companion object {
        private const val STATE_OPENGL = "STATE_OPENGL"
    }

    override val layoutResId = R.layout.fragment_main

    private val walletFragment by lazy { WalletFragment.newInstance() }
    private val dashboardFragment by lazy { DashboardFragment.newInstance() }

    private lateinit var openGlSurfaceView: OpenGLSurfaceView

    private var openGlState: Bundle? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            dashboardRadioButton.isChecked = true
            showDashboardFragment()
        }
        openGlSurfaceView = surfaceView
        restoreGlState(savedInstanceState)

        initBottomBar()
        setupInfoButton()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveGlState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        restoreGlState(savedInstanceState)
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

    private fun initBottomBar() {
        walletRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showWalletFragment()
            }
        }
        dashboardRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showDashboardFragment()
            }
        }
    }

    private fun setupInfoButton() {
        infoButton.setOnClickListener {
            showAboutScreen()
        }
    }

    private fun showWalletFragment() {
        childFragmentManager.transaction {
            replace(R.id.fragmentContainer, walletFragment)
        }
    }

    private fun showDashboardFragment() {
        childFragmentManager.transaction {
            replace(R.id.fragmentContainer, dashboardFragment)
        }
    }

    private fun showAboutScreen() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_mainFragment_to_aboutFragment)
    }
}

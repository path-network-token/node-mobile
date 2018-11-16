package network.path.mobilenode.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.transaction
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.fragment_main.*
import network.path.mobilenode.R
import network.path.mobilenode.domain.entity.ConnectionStatus
import network.path.mobilenode.ui.base.BaseFragment
import network.path.mobilenode.ui.main.dashboard.DashboardFragment
import network.path.mobilenode.ui.main.wallet.WalletFragment
import network.path.mobilenode.ui.opengl.OpenGLSurfaceView
import network.path.mobilenode.utils.observe
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainFragment : BaseFragment() {
    companion object {
        private const val STATE_OPENGL = "STATE_OPENGL"
    }

    override val layoutResId = R.layout.fragment_main

    private val mainViewModel by viewModel<MainViewModel>()

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

        mainViewModel.let {
            it.onViewCreated()
            it.status.observe(this, ::setStatus)
            it.isRunning.observe(this, ::setRunning)
        }
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
    }

    private fun setupInfoButton() {
        infoButton.setOnClickListener {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_mainFragment_to_aboutFragment)
        }
    }

    private fun showDashboardFragment() {
        childFragmentManager.transaction {
            replace(R.id.fragmentContainer, dashboardFragment)
        }
    }

    private fun setStatus(status: ConnectionStatus) {
        openGlSurfaceView.setConnectionStatus(status)
    }

    private fun setRunning(isRunning: Boolean) {
    }
}

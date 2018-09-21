package network.path.mobilenode.ui.main

import android.os.Bundle
import androidx.fragment.app.transaction
import kotlinx.android.synthetic.main.activity_main.*
import network.path.mobilenode.BaseActivity
import network.path.mobilenode.R
import network.path.mobilenode.ui.main.dashboard.DashboardFragment
import network.path.mobilenode.ui.main.wallet.WalletFragment
import org.koin.android.architecture.ext.android.viewModel

class MainActivity : BaseActivity() {

    override val layoutResId = R.layout.activity_main
    override val viewModel by viewModel<MainViewModel>()

    private val walletFragment by lazy { WalletFragment() }
    private val dashboardFragment by lazy { DashboardFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            dashboardRadioButton.isChecked = true
            initFragments()
        }
        initBottomBar()
    }

    private fun initFragments() {
        supportFragmentManager.transaction {
            val containerResId = R.id.fragmentContainer
            add(containerResId, dashboardFragment)
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

    private fun showWalletFragment() {
        supportFragmentManager.transaction {
            replace(R.id.fragmentContainer, walletFragment)
        }
    }

    private fun showDashboardFragment() {
        supportFragmentManager.transaction {
            replace(R.id.fragmentContainer, dashboardFragment)
        }
    }
}
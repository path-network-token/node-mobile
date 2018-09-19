package network.path.mobilenode.ui.main

import android.os.Bundle
import androidx.fragment.app.transaction
import kotlinx.android.synthetic.main.activity_main.*
import network.path.mobilenode.BaseActivity
import network.path.mobilenode.BaseFragment
import network.path.mobilenode.R
import network.path.mobilenode.ui.main.dashboard.DashboardFragment
import network.path.mobilenode.ui.main.wallet.WalletFragment
import org.koin.android.architecture.ext.android.viewModel

class MainActivity : BaseActivity() {

    override val layoutResId = R.layout.activity_main
    override val viewModel by viewModel<MainViewModel>()

    private val walletFragment by lazy { WalletFragment() }
    private val dashboardFragment by lazy { DashboardFragment() }
    private var currentFragment: BaseFragment = dashboardFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        dashboardButton.isChecked = true
        initFragments()
        initBottomBar()
    }

    private fun initFragments() {
        supportFragmentManager.transaction {
            val containerResId = R.id.fragmentContainer
            add(containerResId, walletFragment)
            add(containerResId, dashboardFragment)
            hide(walletFragment)

            addToBackStack(null)
        }
    }

    private fun initBottomBar() {
        walletButton.setOnClickListener {
            if (currentFragment == dashboardFragment) {
                showWalletFragment()
            }
        }

        dashboardButton.setOnClickListener {
            if (currentFragment == walletFragment) {
                showDashboardFragment()
            }
        }
    }

    private fun showWalletFragment() {
        supportFragmentManager.transaction {
            hide(dashboardFragment)
            show(walletFragment)
        }
        currentFragment = walletFragment
    }

    private fun showDashboardFragment() {
        supportFragmentManager.transaction {
            hide(walletFragment)
            show(dashboardFragment)
        }
        currentFragment = dashboardFragment
    }
}
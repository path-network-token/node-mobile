package network.path.mobilenode.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.transaction
import kotlinx.android.synthetic.main.fragment_main.*

import network.path.mobilenode.BaseFragment
import network.path.mobilenode.R
import network.path.mobilenode.ui.main.dashboard.DashboardFragment
import network.path.mobilenode.ui.main.wallet.WalletFragment

class MainFragment : BaseFragment() {

    override val layoutResId = R.layout.fragment_main

    private val walletFragment by lazy { WalletFragment() }
    private val dashboardFragment by lazy { DashboardFragment() }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            dashboardRadioButton.isChecked = true
            initFragments()
        }
        initBottomBar()
    }

    private fun initFragments() {
        fragmentManager!!.transaction {
            add(R.id.fragmentContainer, dashboardFragment)
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
        fragmentManager!!.transaction {
            replace(R.id.fragmentContainer, walletFragment)
        }
    }

    private fun showDashboardFragment() {
        fragmentManager!!.transaction {
            replace(R.id.fragmentContainer, dashboardFragment)
        }
    }
}
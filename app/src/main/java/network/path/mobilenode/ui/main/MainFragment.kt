package network.path.mobilenode.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.transaction
import kotlinx.android.synthetic.main.fragment_main.*
import network.path.mobilenode.R
import network.path.mobilenode.ui.base.BaseFragment
import network.path.mobilenode.ui.main.dashboard.DashboardFragment
import network.path.mobilenode.ui.main.wallet.WalletFragment

class MainFragment : BaseFragment() {
    override val layoutResId = R.layout.fragment_main

    private val walletFragment by lazy { WalletFragment.newInstance() }
    private val dashboardFragment by lazy { DashboardFragment.newInstance() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            dashboardRadioButton.isChecked = true
            showDashboardFragment()
        }
        initBottomBar()
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

    private fun showDashboardFragment() {
        childFragmentManager.transaction {
            replace(R.id.fragmentContainer, dashboardFragment)
        }
    }
}

package network.path.mobilenode.ui.main.dashboard

import network.path.mobilenode.BaseFragment
import network.path.mobilenode.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class DashboardFragment : BaseFragment() {

    override val viewModel by viewModel<DashboardViewModel>()
    override val layoutResId = R.layout.fragment_dashboard
}
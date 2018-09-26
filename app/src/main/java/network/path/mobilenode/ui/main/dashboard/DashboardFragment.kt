package network.path.mobilenode.ui.main.dashboard

import android.os.Bundle
import android.view.View
import network.path.mobilenode.BaseFragment
import network.path.mobilenode.R

class DashboardFragment : BaseFragment() {

    override val layoutResId = R.layout.fragment_dashboard

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupViews() {

    }

    companion object {
        fun newInstance() = DashboardFragment()
    }
}
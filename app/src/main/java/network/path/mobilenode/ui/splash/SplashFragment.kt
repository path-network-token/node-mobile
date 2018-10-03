package network.path.mobilenode.ui.splash

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import network.path.mobilenode.R
import network.path.mobilenode.ui.BaseFragment
import network.path.mobilenode.ui.observe
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashFragment : BaseFragment() {

    override val layoutResId: Int = R.layout.fragment_splash
    private val viewModel by viewModel<SplashViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.let {
            it.onViewCreated()
            it.showIntroScreen.observe(this, ::showIntroScreen)
            it.showMainScreen.observe(this, ::showMainScreen)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    private fun showMainScreen() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_splashFragment_to_mainFragment)
    }

    private fun showIntroScreen() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_splashFragment_to_introFragment)
    }
}
package network.path.mobilenode.ui.splash

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.fragment_splash.*
import network.path.mobilenode.BuildConfig
import network.path.mobilenode.R
import network.path.mobilenode.ui.base.BaseFragment
import network.path.mobilenode.utils.observe
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_splash

    private val viewModel by viewModel<SplashViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        version.text = getString(R.string.label_version, BuildConfig.VERSION_NAME)
        viewModel.let {
            it.onViewCreated()
            it.nextScreen.observe(this, ::showScreen)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    private fun showScreen(nextScreen: SplashViewModel.NextScreen) {
        val actionId = when (nextScreen) {
            SplashViewModel.NextScreen.INTRO -> R.id.action_splashFragment_to_introFragment
            SplashViewModel.NextScreen.MAIN -> R.id.action_splashFragment_to_mainFragment
        }
        NavHostFragment.findNavController(this).navigate(actionId)
    }
}

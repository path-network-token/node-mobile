package network.path.mobilenode.ui.splash

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import network.path.mobilenode.BaseFragment
import network.path.mobilenode.R


class SplashFragment : BaseFragment() {

    override val layoutResId: Int = R.layout.fragment_splash

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler().postDelayed(
                {
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_splashFragment_to_mainFragment)
                }, 5000)
    }
}
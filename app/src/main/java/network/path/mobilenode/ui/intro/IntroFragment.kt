package network.path.mobilenode.ui.intro

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.fragment_intro.*
import network.path.mobilenode.BaseFragment
import network.path.mobilenode.R

class IntroFragment : BaseFragment() {

    override val layoutResId = R.layout.fragment_intro

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activateButton.setOnClickListener {
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_introFragment_to_loadingFragment)
        }

        disclaimerButton.setOnClickListener {
            // TODO: PAN-35
        }
    }
}
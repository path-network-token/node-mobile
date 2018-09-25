package network.path.mobilenode.ui.intro

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_intro.*
import network.path.mobilenode.BaseFragment
import network.path.mobilenode.R

class IntroFragment : BaseFragment() {

    override val layoutResId = R.layout.fragment_intro

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activateButton.setOnClickListener {
            // TODO: PAN-43
        }

        disclaimerButton.setOnClickListener {
            // TODO: PAN-35
        }
    }
}
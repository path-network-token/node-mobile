package network.path.mobilenode.ui.main.wallet

import android.os.Bundle
import network.path.mobilenode.BaseActivity
import network.path.mobilenode.R
import org.koin.android.architecture.ext.android.viewModel

class MainActivity : BaseActivity() {

    override val layoutResId = R.layout.activity_main
    override val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
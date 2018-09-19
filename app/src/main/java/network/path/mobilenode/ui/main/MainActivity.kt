package network.path.mobilenode.ui.main

import android.os.Bundle
import androidx.fragment.app.transaction
import kotlinx.android.synthetic.main.activity_main.*
import network.path.mobilenode.BaseActivity
import network.path.mobilenode.R
import network.path.mobilenode.ui.main.wallet.WalletFragment
import org.koin.android.architecture.ext.android.viewModel

class MainActivity : BaseActivity() {

    override val layoutResId = R.layout.activity_main
    override val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        dashboardButton.isChecked = true
        supportFragmentManager.transaction {
            val containerResId = R.id.fragmentContainer
            add(containerResId, WalletFragment())

            addToBackStack(null)
        }
    }
}
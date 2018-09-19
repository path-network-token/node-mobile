package network.path.mobilenode.ui.main.wallet

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet.*
import network.path.mobilenode.BaseFragment
import network.path.mobilenode.R
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val WALLET_ADDRESS_MAX_LINES = 2 //not working in XML - workaround

class WalletFragment : BaseFragment() {

    override val layoutResId = R.layout.fragment_wallet
    override val viewModel by viewModel<WalletViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        walletAddressInputEditText.setHorizontallyScrolling(false)
        walletAddressInputEditText.maxLines = WALLET_ADDRESS_MAX_LINES
    }
}
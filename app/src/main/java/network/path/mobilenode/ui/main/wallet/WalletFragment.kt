package network.path.mobilenode.ui.main.wallet

import android.os.Bundle
import android.view.View
import android.widget.EditText
import kotlinx.android.synthetic.main.fragment_wallet.*
import network.path.mobilenode.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val WALLET_ADDRESS_MAX_LINES = 2 //not working in XML - workaround

class WalletFragment : BaseFragment() {

    override val layoutResId = R.layout.fragment_wallet
    override val viewModel by viewModel<WalletViewModel>()

    private val storage by lazy { Storage(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        with(walletAddressInputEditText) {
            setHorizontallyScrolling(false)
            maxLines = WALLET_ADDRESS_MAX_LINES
            setText(storage.pathWalletAddress)
            onTextChanged { onWalletAddressConfirmed() }
        }
    }

    private fun onWalletAddressConfirmed() {
        if ((walletAddressInputEditText as EditText).text.isBlank()) {
            walletAddressInputEditText.error = getString(R.string.blank_path_wallet_address_error)
        } else {
            updatePathWalletAddress()
            walletAddressInputEditText.error = null
        }
    }

    private fun updatePathWalletAddress() {
        storage.pathWalletAddress = walletAddressInputEditText.text.toString()
        showToast(requireContext(), R.string.address_saved_toast)
    }
}
package network.path.mobilenode.ui.main.wallet

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet.*
import network.path.mobilenode.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.regex.Pattern

private const val WALLET_ADDRESS_MAX_LINES = 2 //not working in XML - workaround
private const val ETH_ADDRESS_PATTERN = "^0x[a-fA-F0-9]{40}\$"

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
            onTextChanged { onWalletAddressChanged() }
        }

        linkWalletButton.setOnClickListener { onLinkWalletAddressButtonClicked() }
    }

    private fun onWalletAddressChanged() {
        with(walletAddressInputEditText) {
            val walletAddressValidationError = when {
                text.toString().isBlank() -> {
                    linkWalletButton.isEnabled = false
                    getString(R.string.blank_path_wallet_address_error)
                }
                isValidWalletAddress(text.toString()) -> {
                    linkWalletButton.isEnabled = true
                    null
                }
                else -> {
                    linkWalletButton.isEnabled = false
                    getString(R.string.invalid_path_wallet_address_error)
                }
            }

            walletAddressInputLayout.error = walletAddressValidationError
        }
    }

    private fun isValidWalletAddress(text: CharSequence) = Pattern
        .compile(ETH_ADDRESS_PATTERN)
        .matcher(text)
        .matches()

    private fun onLinkWalletAddressButtonClicked() {
        updatePathWalletAddress()
    }

    private fun updatePathWalletAddress() {
        storage.pathWalletAddress = walletAddressInputEditText.text.toString()
        showToast(requireContext(), R.string.address_saved_toast)
    }
}
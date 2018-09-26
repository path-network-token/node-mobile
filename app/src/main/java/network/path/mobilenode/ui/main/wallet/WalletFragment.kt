package network.path.mobilenode.ui.main.wallet

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.fragment_wallet.*
import network.path.mobilenode.*
import org.koin.android.ext.android.inject

private const val WALLET_ADDRESS_MAX_LINES = 2 //not working in XML - workaround
private val ETH_ADDRESS_REGEX = "^0x[a-fA-F0-9]{40}\$".toRegex()

class WalletFragment : BaseFragment() {

    override val layoutResId = R.layout.fragment_wallet

    private val storage by inject<Storage>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            walletAddressInputEditText.setText(storage.pathWalletAddress)
        }
        setupViews()
    }

    private fun setupViews() {
        with(walletAddressInputEditText) {
            setHorizontallyScrolling(false)
            maxLines = WALLET_ADDRESS_MAX_LINES

            onTextChanged { onWalletAddressChanged() }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId in arrayOf(EditorInfo.IME_ACTION_DONE, EditorInfo.IME_NULL)) {
                    updatePathWalletAddress()
                }
                false
            }
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

    private fun isValidWalletAddress(text: CharSequence) = ETH_ADDRESS_REGEX.matches(text)

    private fun onLinkWalletAddressButtonClicked() {
        updatePathWalletAddress()
    }

    private fun updatePathWalletAddress() {
        storage.pathWalletAddress = walletAddressInputEditText.text.toString()
        showToast(requireContext(), R.string.address_saved_toast)
    }


    companion object {
        fun newInstnace() = WalletFragment()
    }
}
package network.path.mobilenode.ui.main.wallet

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.fragment_wallet.*
import network.path.mobilenode.R
import network.path.mobilenode.domain.PathStorage
import network.path.mobilenode.ui.base.BaseFragment
import network.path.mobilenode.utils.onTextChanged
import network.path.mobilenode.utils.showToast
import org.koin.android.ext.android.inject

class WalletFragment : BaseFragment() {
    companion object {
        private const val WALLET_ADDRESS_MAX_LINES = 2 //not working in XML - workaround

        private val ETH_ADDRESS_REGEX = "^0x[a-fA-F0-9]{40}\$".toRegex()

        fun newInstance() = WalletFragment()
    }

    override val layoutResId = R.layout.fragment_wallet

    private val storage by inject<PathStorage>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            walletAddressInputEditText.setText(storage.walletAddress)
        }
        setupViews()
    }

    private fun setupViews() {
        with(walletAddressInputEditText) {
            setHorizontallyScrolling(false)
            maxLines = WALLET_ADDRESS_MAX_LINES

            onTextChanged { onWalletAddressChanged(it.toString()) }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId in arrayOf(EditorInfo.IME_ACTION_DONE, EditorInfo.IME_NULL)) {
                    updatePathWalletAddress()
                }
                false
            }
        }
        linkWalletButton.setOnClickListener { updatePathWalletAddress() }
    }

    private fun onWalletAddressChanged(text: String) {
        val validationError = when {
            text.isBlank() -> getString(R.string.blank_path_wallet_address_error)
            ETH_ADDRESS_REGEX.matches(text) -> null
            else -> getString(R.string.invalid_path_wallet_address_error)
        }

        linkWalletButton.isEnabled = validationError == null
        walletAddressInputLayout.error = validationError
    }

    private fun updatePathWalletAddress() {
        if (linkWalletButton.isEnabled) {
            storage.walletAddress = walletAddressInputEditText.text.toString()
            showToast(requireContext(), R.string.address_saved_toast)
        }
    }
}

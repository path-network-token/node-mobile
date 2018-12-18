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
import org.web3j.crypto.Hash
import org.web3j.utils.Numeric

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
            onWalletAddressChanged(storage.walletAddress)
        }
        setupViews()
    }

    private fun setupViews() {
        with(walletAddressInputEditText) {
            setHorizontallyScrolling(false)
            maxLines = WALLET_ADDRESS_MAX_LINES

            onTextChanged { onWalletAddressChanged(it) }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId in arrayOf(EditorInfo.IME_ACTION_DONE, EditorInfo.IME_NULL)) {
                    updatePathWalletAddress()
                }
                false
            }
        }
        linkWalletButton.setOnClickListener { updatePathWalletAddress() }
    }

    private fun onWalletAddressChanged(text: CharSequence) {
        val validationError = when {
            text.isBlank() -> getString(R.string.blank_path_wallet_address_error)
            isValid(text) -> null
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

    private fun isValid(address: CharSequence) =
            Numeric.prependHexPrefix(address.toString()) == checkedAddress(address)

    private fun checkedAddress(address: CharSequence): String {
        val cleanAddress = Numeric.cleanHexPrefix(address.toString()).toLowerCase()

        val sb = StringBuilder()
        val hash = Hash.sha3String(cleanAddress)
        val hashChars = hash.substring(2).toCharArray()

        val chars = cleanAddress.toCharArray()
        for (i in chars.indices) {
            val c = if (Character.digit(hashChars[i], 16) and 0xFF > 7) {
                Character.toUpperCase(chars[i])
            } else {
                Character.toLowerCase(chars[i])
            }
            sb.append(c)
        }
        return Numeric.prependHexPrefix(sb.toString())
    }
}

package network.path.mobilenode.ui.main.wallet

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.fragment_wallet.*
import network.path.mobilenode.Constants
import network.path.mobilenode.R
import network.path.mobilenode.domain.PathStorage
import network.path.mobilenode.ui.base.BaseFragment
import network.path.mobilenode.utils.TranslationFractionProperty
import network.path.mobilenode.utils.onTextChanged
import org.koin.android.ext.android.inject
import org.web3j.crypto.Hash
import org.web3j.utils.Numeric

class WalletFragment : BaseFragment() {
    companion object {
        private const val WALLET_ADDRESS_MAX_LINES = 2 //not working in XML - workaround

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
        setupEditViews()
        setupViewViews()

        val isEdit = !hasAddress()
        setMode(isEdit)
    }

    private fun setMode(isEdit: Boolean) {
        val editVisibility = if (isEdit) View.VISIBLE else View.INVISIBLE
        setupWalletTextView.visibility = editVisibility
        headerEditDivider.visibility = editVisibility
        walletPrompt.visibility = editVisibility
        walletAddressInputEditText.visibility = editVisibility
        walletAddressInputLayout.visibility = editVisibility
        linkWalletButton.visibility = editVisibility

        val viewVisibility = if (isEdit) View.INVISIBLE else View.VISIBLE
        walletTextView.visibility = viewVisibility
        headerViewDivider.visibility = viewVisibility
        walletAddressViewLayout.visibility = viewVisibility
        walletAddressTextView.visibility = viewVisibility
        editButton.visibility = viewVisibility

        if (isEdit) {
            animateEditIn()
        } else {
            animateViewIn()
        }
    }

    private fun setupEditViews() {
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

    private fun setupViewViews() {
        walletAddressTextView.setText(storage.walletAddress)
        editButton.setOnClickListener { setMode(true) }
    }

    private fun animateEditIn() {
        val offset = 500f
        headerEditDivider.translationY = offset
        setupWalletTextView.translationY = offset
        walletPrompt.translationY = offset
        walletAddressInputLayout.alpha = 0f
        linkWalletButton.alpha = 0f

        val dividerAnimation = ObjectAnimator.ofFloat(headerEditDivider, "translationY", offset, 0f)
        val headerAnimation = ObjectAnimator.ofFloat(setupWalletTextView, "translationY", offset, 0f)
        val promptAnimation = ObjectAnimator.ofFloat(walletPrompt, "translationY", offset, 0f)
        val slideSet = AnimatorSet()
        slideSet.playTogether(dividerAnimation, headerAnimation, promptAnimation)
        slideSet.interpolator = AccelerateDecelerateInterpolator()
        slideSet.duration = 250L

        val textAnimation = ObjectAnimator.ofFloat(walletAddressInputLayout, "alpha", 0f, 1f)
        val buttonAnimation = ObjectAnimator.ofFloat(linkWalletButton, "alpha", 0f, 1f)
        val alphaSet = AnimatorSet()
        alphaSet.playTogether(textAnimation, buttonAnimation)
        alphaSet.interpolator = AccelerateDecelerateInterpolator()
        alphaSet.duration = 250L

        val set = AnimatorSet()
        set.playSequentially(slideSet, alphaSet)
        set.start()
    }

    private fun animateViewIn() {
        headerViewDivider.translationX = -1000f
        walletTextView.translationX = -1000f
        walletAddressViewLayout.translationY = 2000f
        editButton.translationY = 2000f

        val dividerAnimation = ObjectAnimator.ofFloat(headerViewDivider, TranslationFractionProperty(false), -0.5f, 0f)
        dividerAnimation.interpolator = AccelerateDecelerateInterpolator()
        dividerAnimation.duration = 250L

        val headerAnimation = ObjectAnimator.ofFloat(walletTextView, TranslationFractionProperty(false), -0.5f, 0f)
        headerAnimation.interpolator = AccelerateDecelerateInterpolator()
        headerAnimation.duration = 250L

        val textAnimation = ObjectAnimator.ofFloat(walletAddressViewLayout, TranslationFractionProperty(true), 0.5f, 0f)
        val buttonAnimation = ObjectAnimator.ofFloat(editButton, TranslationFractionProperty(true), 0.5f, 0f)
        val bottomSet = AnimatorSet()
        bottomSet.playTogether(textAnimation, buttonAnimation)
        bottomSet.interpolator = AccelerateDecelerateInterpolator()
        bottomSet.duration = 250L

        val set = AnimatorSet()
        set.playSequentially(dividerAnimation, headerAnimation, bottomSet)
        set.start()
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
            setMode(false)
        }
    }

    private fun hasAddress() = storage.walletAddress != Constants.PATH_DEFAULT_WALLET_ADDRESS

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

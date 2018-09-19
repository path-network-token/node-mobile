package network.path.mobilenode

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

fun EditText.onTextChanged(callback: (CharSequence) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
            callback(text)
        }

        override fun afterTextChanged(editable: Editable?) = Unit

        override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) =
            Unit
    })
}
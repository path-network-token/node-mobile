package network.path.mobilenode.ui.main.jobreport

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.res.use
import kotlinx.android.synthetic.main.job_type_button.view.*
import network.path.mobilenode.R

class JobTypeButton
@SuppressLint("Recycle") //obtainStyledAttributes recycled in ktx
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.job_type_button, this)

        context.obtainStyledAttributes(attrs, R.styleable.JobTypeButton, defStyleAttr, defStyleRes)
            .use {
                val isSelected = it.getBoolean(R.styleable.JobTypeButton_is_selected, false)
                val text = it.getString(R.styleable.JobTypeButton_text)


                jobTypeTextView.background = getDrawable(
                    context,
                    if (isSelected) R.drawable.selected_background else R.color.dark_slate_blue
                )

                jobTypeTextView.text = text
            }
    }
}
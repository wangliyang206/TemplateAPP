package com.template.core.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.template.core.R

/**
 * 状态卡片：Loading / Empty / Error
 */
class StateCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val ivIcon: ImageView
    private val tvMessage: TextView
    private val btnRetry: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_state_card, this, true)
        ivIcon = findViewById(R.id.iv_state_icon)
        tvMessage = findViewById(R.id.tv_state_message)
        btnRetry = findViewById(R.id.btn_state_retry)
    }

    fun show(iconRes: Int, message: String, onRetry: (() -> Unit)? = null) {
        visibility = VISIBLE
        ivIcon.setImageResource(iconRes)
        tvMessage.text = message
        if (onRetry != null) {
            btnRetry.visibility = VISIBLE
            btnRetry.setOnClickListener { onRetry() }
        } else {
            btnRetry.visibility = GONE
        }
    }
}

package com.template.core.ext

import android.view.View

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.setOnSingleClickListener(interval: Long = 800L, block: (View) -> Unit) {
    var lastClick = 0L
    setOnClickListener {
        val now = System.currentTimeMillis()
        if (now - lastClick > interval) {
            lastClick = now
            block(it)
        }
    }
}

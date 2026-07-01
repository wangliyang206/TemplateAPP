package com.template.core.ext

import android.content.Context
import android.widget.Toast

fun Context.toastShort(message: String?) {
    message?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
}

fun Context.toastLong(message: String?) {
    message?.let { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
}

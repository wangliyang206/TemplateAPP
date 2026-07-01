package com.template.core.ext

fun String?.orEmpty(default: String = ""): String = this ?: default

fun String.isPhone(): Boolean {
    return matches(Regex("^1[3-9]\\d{9}$"))
}

fun String.isEmail(): Boolean {
    return matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"))
}

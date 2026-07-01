package com.template.core.ext

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

class EventObserver<T>(private val onEventUnhandled: (T) -> Unit) : Observer<T> {
    override fun onChanged(value: T) {
        if (value is Event<*> && value.hasBeenHandled) return
        if (value is Event<*>) value.getContentIfNotHandled()
        onEventUnhandled(value)
    }
}

open class Event<out T>(private val content: T) {
    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}

fun <T> LiveData<T>.observeEvent(owner: LifecycleOwner, onEvent: (T) -> Unit) {
    observe(owner, EventObserver(onEvent))
}

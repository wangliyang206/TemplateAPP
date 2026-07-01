package com.template.core.mmkv

import com.tencent.mmkv.MMKV

object MMKVManager {

    private val mmkv: MMKV by lazy { MMKV.defaultMMKV() }

    fun putString(key: String, value: String) = mmkv.encode(key, value)
    fun getString(key: String, default: String = ""): String = mmkv.decodeString(key, default) ?: default

    fun putInt(key: String, value: Int) = mmkv.encode(key, value)
    fun getInt(key: String, default: Int = 0): Int = mmkv.decodeInt(key, default)

    fun putBoolean(key: String, value: Boolean) = mmkv.encode(key, value)
    fun getBoolean(key: String, default: Boolean = false): Boolean = mmkv.decodeBool(key, default)

    fun remove(key: String) = mmkv.removeValueForKey(key)
    fun clear() = mmkv.clearAll()
}

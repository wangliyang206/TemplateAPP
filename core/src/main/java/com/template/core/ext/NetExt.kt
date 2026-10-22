package com.template.core.ext

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * 网络状态判断。
 *
 * 需要 `ACCESS_NETWORK_STATE` 权限（模板的 AndroidManifest 已声明）。
 * 只判断"有没有可用网络"，不判断"能不能通到你的服务端"——后者要发真实请求。
 */
fun Context.isNetworkAvailable(): Boolean {
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val network = manager.activeNetwork ?: return false
    val capabilities = manager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

/** 是否在计费网络（移动数据）：下载大文件 / 自动播放视频前可据此提示用户。 */
fun Context.isMeteredNetwork(): Boolean {
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    return manager.isActiveNetworkMetered
}

package com.template.core.network

import android.content.Context
import android.os.Build
import android.provider.Settings
import timber.log.Timber
import java.util.UUID

/**
 * 设备信息采集器
 *
 * 负责收集设备信息并填充到 [NetworkConfig.clientInfo]。
 *
 * 采集范围**刻意只包含无需危险权限的字段**：
 * cell / simid 受 READ_PHONE_STATE 管控，属应用商店敏感权限，
 * 且 Android 10+ 对 line1Number / simSerialNumber 已基本不可得，
 * 声明该权限审核成本高、收益极低，因此模板默认保持为空串。
 * 若业务确需，请自行声明权限并在此扩展采集。
 */
object DeviceInfoCollector {

    /**
     * 初始化设备信息（Application.onCreate 中调用）。
     *
     * 只读取无需权限的字段：ANDROID_ID、系统版本、版本号、屏幕分辨率。
     */
    fun init(context: Context) {
        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID,
        ) ?: UUID.randomUUID().toString()

        NetworkConfig.clientId = deviceId

        val displayMetrics = context.resources.displayMetrics

        NetworkConfig.clientInfo = ClientInfo(
            cell = "",
            deviceid = deviceId,
            simid = "",
            os = "Android",
            osver = Build.VERSION.RELEASE,
            vercode = getVerCode(context),
            vername = getVerName(context),
            ppiheight = displayMetrics.heightPixels.toString(),
            ppiwidth = displayMetrics.widthPixels.toString(),
        )
    }

    // ========== 内部方法 ==========

    private fun getVerCode(context: Context): String = safeRead("versionCode", "") {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pInfo.longVersionCode.toString()
        } else {
            @Suppress("DEPRECATION")
            pInfo.versionCode.toString()
        }
    }

    private fun getVerName(context: Context): String = safeRead("versionName", "") {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
    }

    /**
     * 读取设备信息的安全包装。
     *
     * 之所以 catch 的是 `Exception` 而不是具体类型：这些字段来自不同厂商 ROM 的
     * PackageManager 实现，可能抛 SecurityException、RuntimeException 甚至厂商自定义异常。
     * 设备信息只用于埋点上报，任何一个字段读不到都不该影响启动，
     * 因此统一降级为空字符串 —— 但**必须打日志**，否则线上排查时无法区分
     * "没有值" 与 "读取被拒绝"。
     */
    @Suppress("TooGenericExceptionCaught")
    private inline fun <T> safeRead(field: String, fallback: T, block: () -> T): T = try {
        block()
    } catch (e: Exception) {
        Timber.d(e, "读取设备信息失败: %s", field)
        fallback
    }
}

package com.template.core.network

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import timber.log.Timber
import java.util.UUID

/**
 * 设备信息采集器
 *
 * 负责收集设备信息并填充到 NetworkConfig.clientInfo。
 * - 无权限时：cell/simid 传空字符串，其余字段正常填充
 * - 有权限后：调用 refreshPhoneInfo() 更新 cell/simid 为真实数据
 */
object DeviceInfoCollector {

    /**
     * 初始化全量设备信息（Application.onCreate 中调用）
     *
     * 不需要权限的字段直接填充，需要权限的字段尝试读取，
     * 无权限时自动降级为空字符串。
     */
    fun init(context: Context) {
        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID,
        ) ?: UUID.randomUUID().toString()

        NetworkConfig.clientId = deviceId

        val displayMetrics = context.resources.displayMetrics

        NetworkConfig.clientInfo = ClientInfo(
            cell = getPhoneNumber(context),
            deviceid = deviceId,
            simid = getSimSerialNumber(context),
            os = "Android",
            osver = Build.VERSION.RELEASE,
            vercode = getVerCode(context),
            vername = getVerName(context),
            ppiheight = displayMetrics.heightPixels.toString(),
            ppiwidth = displayMetrics.widthPixels.toString(),
        )
    }

    /**
     * 权限授予后刷新 cell 和 simid（在 Activity 中调用）
     *
     * 只更新需要权限的字段，其余字段保持不变。
     */
    fun refreshPhoneInfo(context: Context) {
        val current = NetworkConfig.clientInfo
        NetworkConfig.clientInfo = current.copy(
            cell = getPhoneNumber(context),
            simid = getSimSerialNumber(context),
        )
    }

    /** 检查是否拥有读取手机信息的权限 */
    fun hasPhonePermission(context: Context): Boolean = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_PHONE_STATE,
    ) == PackageManager.PERMISSION_GRANTED

    // ========== 内部方法 ==========

    private fun getPhoneNumber(context: Context): String = safeRead("phoneNumber", "") {
        if (!hasPhonePermission(context)) return@safeRead ""
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        @Suppress("MissingPermission")
        tm?.line1Number ?: ""
    }

    private fun getSimSerialNumber(context: Context): String = safeRead("simSerial", "") {
        if (!hasPhonePermission(context)) return@safeRead ""
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        @Suppress("MissingPermission")
        tm?.simSerialNumber ?: ""
    }

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
     * TelephonyManager / PackageManager 实现，可能抛 SecurityException、RuntimeException
     * 甚至厂商自定义异常。设备信息只用于埋点上报，任何一个字段读不到都不该影响启动，
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

package com.template.core.network

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
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
            Settings.Secure.ANDROID_ID
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
            ppilheight = displayMetrics.heightPixels.toString(),
            ppiwidth = displayMetrics.widthPixels.toString()
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
            simid = getSimSerialNumber(context)
        )
    }

    /** 检查是否拥有读取手机信息的权限 */
    fun hasPhonePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // ========== 内部方法 ==========

    private fun getPhoneNumber(context: Context): String {
        return try {
            if (!hasPhonePermission(context)) return ""
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            @Suppress("MissingPermission")
            tm?.line1Number ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun getSimSerialNumber(context: Context): String {
        return try {
            if (!hasPhonePermission(context)) return ""
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            @Suppress("MissingPermission")
            tm?.simSerialNumber ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun getVerCode(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toString()
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun getVerName(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}

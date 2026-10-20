package com.template.core.permission

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.template.core.ext.toastShort

/**
 * 权限申请工具类 — 封装 XXPermissions
 *
 * 用法：
 *
 * // Activity 或 Fragment 中：
 * private val permissionHelper = PermissionHelper { granted ->
 *     if (granted) openCamera()
 * }
 *
 * // 申请相机权限
 * permissionHelper.requestCamera(this)
 *
 * // 申请存储权限（Android 13+ 按需适配）
 * permissionHelper.requestStorage(this)
 */
class PermissionHelper(private val onResult: (granted: Boolean) -> Unit) {

    /** 申请相机权限 */
    fun requestCamera(activity: FragmentActivity) {
        XXPermissions.with(activity)
            .permission(Manifest.permission.CAMERA)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    onResult(allGranted)
                }
                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    if (doNotAskAgain) {
                        activity.toastShort("请在设置中开启相机权限")
                    } else {
                        activity.toastShort("需要相机权限才能拍照")
                    }
                    onResult(false)
                }
            })
    }

    /** 申请存储权限（兼容 Android 10+） */
    fun requestStorage(activity: FragmentActivity) {
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        XXPermissions.with(activity)
            .permission(permissions)
            .request(object : OnPermissionCallback {
                override fun onGranted(granted: MutableList<String>, allGranted: Boolean) {
                    onResult(allGranted)
                }
                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    activity.toastShort("需要存储权限才能访问文件")
                    onResult(false)
                }
            })
    }

    /** 申请定位权限 */
    fun requestLocation(activity: FragmentActivity) {
        XXPermissions.with(activity)
            .permission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .request(object : OnPermissionCallback {
                override fun onGranted(granted: MutableList<String>, allGranted: Boolean) {
                    onResult(allGranted)
                }
                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    activity.toastShort("需要定位权限")
                    onResult(false)
                }
            })
    }

    /** 申请手机信息权限（读取手机号、SIM 卡信息） */
    fun requestPhone(activity: FragmentActivity) {
        val permissions = mutableListOf(Manifest.permission.READ_PHONE_STATE)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_PHONE_NUMBERS)
        }
        XXPermissions.with(activity)
            .permission(permissions)
            .request(object : OnPermissionCallback {
                override fun onGranted(granted: MutableList<String>, allGranted: Boolean) {
                    onResult(allGranted)
                }
                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    if (doNotAskAgain) {
                        activity.toastShort("请在设置中开启手机信息权限")
                    }
                    onResult(false)
                }
            })
    }

    companion object {
        /** 快速检查权限是否已授予 */
        fun isGranted(activity: FragmentActivity, vararg permissions: String): Boolean = permissions.all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}

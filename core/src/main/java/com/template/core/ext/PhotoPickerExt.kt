package com.template.core.ext

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

/**
 * 系统相册选图的统一封装（Android Photo Picker）。
 *
 * 关键点（很多人会绕弯路）：
 * - **不需要任何存储权限**。PhotoPicker 由系统进程提供选择界面，App 只拿到用户选中的 Uri；
 *   `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE` 都不必申请，应用商店审核也不会因此加项。
 * - 低版本（< Android 11 且没装 MediaProvider 更新）会自动回落到 `GET_CONTENT`，行为一致。
 * - 返回的 Uri 是**临时授权**：进程存活期内可读；要长期使用请复制到 App 私有目录再落库。
 *
 * ```kotlin
 * // 必须在 Fragment 初始化阶段（onCreate 之前）注册
 * private val pickPhoto = registerPhotoPicker { uri -> uri?.let(::showPreview) }
 *
 * // 点击时：
 * pickPhoto.pickImage()          // 只要图片
 * pickPhoto.pickVideo()          // 只要视频
 * ```
 */
fun Fragment.registerPhotoPicker(onPicked: (Uri?) -> Unit): ActivityResultLauncher<PickVisualMediaRequest> =
    registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> onPicked(uri) }

/** 打开系统相册选一张图片 */
fun ActivityResultLauncher<PickVisualMediaRequest>.pickImage() {
    launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
}

/** 打开系统相册选一段视频 */
fun ActivityResultLauncher<PickVisualMediaRequest>.pickVideo() {
    launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
}

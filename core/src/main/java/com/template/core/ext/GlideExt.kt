package com.template.core.ext

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import java.util.concurrent.ConcurrentHashMap

/**
 * 本地资源协议前缀：`res://drawable名` 会被解析成 drawable 资源 id 交给 Glide 加载。
 *
 * 用途：Mock 数据不想依赖外网图床（picsum 等在国内不稳），可以直接指向打包进 APK 的图片，
 * 模板开箱即用不联网也能看到完整 UI。示例：`"res://banner_1"`。
 */
private const val RES_PREFIX = "res://"

/**
 * res:// 名称 → 资源 id 的缓存。getIdentifier 内部有反射开销，轮播图反复 bind 时值得缓存。
 * 只存 String→Int，不持有 Context，无泄漏风险。
 */
private val resIdCache = ConcurrentHashMap<String, Int>()

/**
 * 解析 Glide 加载模型：`res://xxx` 转成本地 drawable 资源 id，其余原样当 URL。
 * 资源不存在时返回 null，Glide 会走 error 占位图，不会崩溃。
 */
private fun resolveModel(imageView: ImageView, url: String?): Any? = when {
    url.isNullOrBlank() -> null
    !url.startsWith(RES_PREFIX) -> url
    else -> resolveDrawableId(imageView, url.removePrefix(RES_PREFIX))
}

/** 把 drawable 名解析成资源 id，找不到返回 null。命中缓存时零反射开销。 */
private fun resolveDrawableId(imageView: ImageView, name: String): Int? = resIdCache[name] ?: imageView.resources
    .getIdentifier(name, "drawable", imageView.context.packageName)
    .takeIf { it != 0 }
    ?.also { resIdCache[name] = it }

/**
 * 加载图片
 * @param url 图片地址，支持 `res://drawable名` 本地资源协议
 * @param placeholder 占位图；不为 0 时同时作为加载失败兜底图，避免网络异常后一片空白
 */
fun ImageView.load(url: String?, placeholder: Int = 0) {
    val request = Glide.with(context).load(resolveModel(this, url))
    if (placeholder != 0) {
        request.placeholder(placeholder).error(placeholder)
    }
    request.into(this)
}

/**
 * 加载圆形图片
 * @param url 图片地址，支持 `res://drawable名` 本地资源协议
 * @param placeholder 占位图；不为 0 时同时作为加载失败兜底图
 */
fun ImageView.loadCircle(url: String?, placeholder: Int = 0) {
    val options = RequestOptions().apply {
        if (placeholder != 0) {
            placeholder(placeholder)
            error(placeholder)
        }
        transform(CircleCrop())
    }
    Glide.with(context).load(resolveModel(this, url)).apply(options).into(this)
}

/**
 * 加载圆角图片
 * @param url 图片地址，支持 `res://drawable名` 本地资源协议
 * @param radius 圆角半径
 * @param placeholder 占位图；不为 0 时同时作为加载失败兜底图
 */
fun ImageView.loadRounded(url: String?, radius: Int, placeholder: Int = 0) {
    val options = RequestOptions().apply {
        if (placeholder != 0) {
            placeholder(placeholder)
            error(placeholder)
        }
        transform(RoundedCorners(radius))
    }
    Glide.with(context).load(resolveModel(this, url)).apply(options).into(this)
}

/**
 * 加载圆形图片，如果图片地址为空则显示占位图
 * @param url 图片地址，支持 `res://drawable名` 本地资源协议
 * @param drawableRes 占位图资源
 */
fun ImageView.loadCircleOrDrawable(url: String?, drawableRes: Int) {
    if (!url.isNullOrEmpty()) {
        loadCircle(url, drawableRes)
    } else {
        setImageResource(drawableRes)
    }
}

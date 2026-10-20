package com.template.core.ext

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

/**
 * 加载图片
 * @param url 图片地址
 * @param placeholder 占位图
 */
fun ImageView.load(url: String?, placeholder: Int = 0) {
    val request = Glide.with(context).load(url)
    if (placeholder != 0) {
        request.placeholder(placeholder)
    }
    request.into(this)
}

/**
 * 加载圆形图片
 * @param url 图片地址
 * @param placeholder 占位图
 */
fun ImageView.loadCircle(url: String?, placeholder: Int = 0) {
    val options = RequestOptions().apply {
        if (placeholder != 0) placeholder(placeholder)
        transform(CircleCrop())
    }
    Glide.with(context).load(url).apply(options).into(this)
}

/**
 * 加载圆角图片
 * @param url 图片地址
 * @param radius 圆角半径
 * @param placeholder 占位图
 */
fun ImageView.loadRounded(url: String?, radius: Int, placeholder: Int = 0) {
    val options = RequestOptions().apply {
        if (placeholder != 0) placeholder(placeholder)
        transform(RoundedCorners(radius))
    }
    Glide.with(context).load(url).apply(options).into(this)
}

/**
 * 加载圆形图片，如果图片地址为空则显示占位图
 * @param url 图片地址
 * @param drawableRes 占位图资源
 */
fun ImageView.loadCircleOrDrawable(url: String?, drawableRes: Int) {
    if (!url.isNullOrEmpty()) {
        loadCircle(url, drawableRes)
    } else {
        setImageResource(drawableRes)
    }
}

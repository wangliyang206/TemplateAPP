package com.template.app.ui.adapter

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.template.app.data.model.Banner
import com.template.core.ext.loadRounded
import com.youth.banner.adapter.BannerAdapter

/**
 * 轮播图 ViewHolder
 */
class BannerViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

/**
 * 轮播广告适配器 - 基于 youth5201314 Banner 库
 *
 * 继承 BannerAdapter，使用 ImageView 作为 item
 */
class BannerImageAdapter(banners: List<Banner>) : BannerAdapter<Banner, BannerViewHolder>(banners) {

    /**
     * 点击回调
     */
    var onItemClick: ((Banner) -> Unit)? = null

    /**
     * 图片圆角半径（dp）
     */
    var roundedRadiusDp: Int = 8

    override fun onCreateHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        return BannerViewHolder(imageView)
    }

    override fun onBindView(holder: BannerViewHolder, data: Banner, position: Int, size: Int) {
        // 使用圆角加载图片
        holder.imageView.loadRounded(data.imageUrl, roundedRadiusDp)
        holder.imageView.setOnClickListener {
            onItemClick?.invoke(data)
        }
    }
}
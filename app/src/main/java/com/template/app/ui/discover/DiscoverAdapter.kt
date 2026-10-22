package com.template.app.ui.discover

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.template.app.R
import com.template.app.data.model.Category
import com.template.app.data.model.RankArticle
import com.template.app.databinding.ItemCategoryCardBinding
import com.template.app.databinding.ItemCategoryRowBinding
import com.template.app.databinding.ItemRankBinding
import com.template.app.databinding.ItemSectionHeaderBinding
import com.template.core.ext.dp2px

/**
 * 发现页 Adapter —— **多 ItemType** 的参考实现。
 *
 * 分类、排行、板块标题共用一个 RecyclerView：靠 `getItemViewType` 分派，
 * 避免 NestedScrollView 套 RecyclerView 带来的滑动冲突与高度测量问题。
 */
class DiscoverAdapter(
    private val onCategoryClick: (Category) -> Unit,
    private val onRankClick: (RankArticle) -> Unit,
) : ListAdapter<DiscoverItem, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is DiscoverItem.Section -> TYPE_SECTION
        is DiscoverItem.CategoryRow -> TYPE_CATEGORY_ROW
        is DiscoverItem.Rank -> TYPE_RANK
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SECTION -> SectionHolder(ItemSectionHeaderBinding.inflate(inflater, parent, false))
            TYPE_CATEGORY_ROW -> CategoryRowHolder(ItemCategoryRowBinding.inflate(inflater, parent, false))
            else -> RankHolder(ItemRankBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is DiscoverItem.Section -> (holder as SectionHolder).bind(item)
            is DiscoverItem.CategoryRow -> (holder as CategoryRowHolder).bind(item, onCategoryClick)
            is DiscoverItem.Rank -> (holder as RankHolder).bind(item, onRankClick)
        }
    }

    private class SectionHolder(private val binding: ItemSectionHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DiscoverItem.Section) {
            binding.tvSectionTitle.setText(
                when (item.type) {
                    SectionType.CATEGORY -> R.string.discover_section_category
                    SectionType.RANK -> R.string.discover_section_rank
                },
            )
        }
    }

    /**
     * 分类行：内部动态填充卡片。
     * 卡片宽度用 `weight = 1` 均分，所以列数变了也不用改布局文件。
     */
    private class CategoryRowHolder(private val binding: ItemCategoryRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DiscoverItem.CategoryRow, onClick: (Category) -> Unit) {
            val row = binding.categoryRow
            row.removeAllViews()
            val context = row.context
            val gap = context.dp2px(GAP_DP.toFloat())
            val inflater = LayoutInflater.from(context)

            item.categories.forEachIndexed { index, category ->
                val card = ItemCategoryCardBinding.inflate(inflater, row, false)
                card.tvName.text = category.name
                card.tvId.text = context.getString(R.string.category_id_tag, category.id)
                card.root.layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f,
                ).apply {
                    // 最后一列不留右边距，否则整行会视觉偏左
                    if (index != item.categories.lastIndex) marginEnd = gap
                }
                card.root.setOnClickListener { onClick(category) }
                row.addView(card.root)
            }
        }
    }

    private class RankHolder(private val binding: ItemRankBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DiscoverItem.Rank, onClick: (RankArticle) -> Unit) {
            val context = binding.root.context
            // 走字符串资源而不是 toString()：名次在阿拉伯语等区域需要本地化数字
            binding.tvPosition.text = context.getString(R.string.discover_rank_position, item.position)
            // 前三名用主题色突出，其余保持灰色
            binding.tvPosition.setTextColor(
                if (item.position <= TOP_HIGHLIGHT) {
                    context.getColor(R.color.primary)
                } else {
                    context.getColor(R.color.text_hint)
                },
            )
            binding.tvTitle.text = item.data.article.title
            binding.tvReads.text = context.getString(R.string.discover_rank_reads, item.data.reads)
            binding.root.setOnClickListener { onClick(item.data) }
        }
    }

    private companion object {
        const val TYPE_SECTION = 0
        const val TYPE_CATEGORY_ROW = 1
        const val TYPE_RANK = 2

        /** 卡片间距（dp） */
        const val GAP_DP = 10

        /** 前 N 名高亮 */
        const val TOP_HIGHLIGHT = 3

        val DIFF = object : DiffUtil.ItemCallback<DiscoverItem>() {
            override fun areItemsTheSame(oldItem: DiscoverItem, newItem: DiscoverItem): Boolean = when {
                oldItem is DiscoverItem.Section && newItem is DiscoverItem.Section ->
                    oldItem.type == newItem.type
                oldItem is DiscoverItem.CategoryRow && newItem is DiscoverItem.CategoryRow ->
                    oldItem.categories.map { it.id } == newItem.categories.map { it.id }
                oldItem is DiscoverItem.Rank && newItem is DiscoverItem.Rank ->
                    oldItem.data.article.id == newItem.data.article.id
                else -> false
            }

            override fun areContentsTheSame(oldItem: DiscoverItem, newItem: DiscoverItem): Boolean = oldItem == newItem
        }
    }
}

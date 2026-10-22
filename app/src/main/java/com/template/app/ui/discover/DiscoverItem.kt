package com.template.app.ui.discover

import com.template.app.data.model.Category
import com.template.app.data.model.RankArticle

/**
 * 发现页列表项。
 *
 * 用 sealed interface 而不是"三个平行的 List"：
 * 一个 RecyclerView 承载多个板块时，把板块差异收敛到类型里，
 * Adapter 通过 `getItemViewType` 分派，`when` 分支由编译器保证穷尽。
 */
sealed interface DiscoverItem {

    /** 板块标题。用枚举而不是 String：ViewModel 不该持有 Context 去取字符串资源 */
    data class Section(val type: SectionType) : DiscoverItem

    /** 一行分类卡片（每行 2 个，由 ViewModel 切块） */
    data class CategoryRow(val categories: List<Category>) : DiscoverItem

    /** 排行条目，position 是名次（从 1 开始） */
    data class Rank(val position: Int, val data: RankArticle) : DiscoverItem
}

/** 发现页板块类型 */
enum class SectionType {
    CATEGORY,
    RANK,
}

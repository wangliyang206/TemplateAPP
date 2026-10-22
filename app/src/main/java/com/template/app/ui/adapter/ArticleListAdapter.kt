package com.template.app.ui.adapter

import com.template.app.data.model.Article
import com.template.app.databinding.ItemArticleBinding
import com.template.core.base.BaseListAdapter

/**
 * 非分页的文章列表 Adapter —— 收藏页 / 浏览历史页。
 *
 * 为什么要有两个文章 Adapter：
 * - [ArticleAdapter] 是 `PagingDataAdapter`，数据源是 `Flow<PagingData<Article>>`，用于资讯流、搜索、分类；
 * - 本类是 [BaseListAdapter]（`ListAdapter`），数据源是普通的 `Flow<List<Article>>`，用于 Room 观察出来的定长列表。
 *
 * 两种 Adapter 都靠 DiffUtil 增量更新，但**不要为了"统一"合并**：
 * 把分页列表塞进 `ListAdapter` 会丢掉边滚边加载的能力；
 * 反过来把上限 200 条的收藏做成 PagingSource 则是纯粹的过度设计。
 *
 * 差异计算复用 [ArticleDiffCallback]，与分页版本保持一致，避免同一篇文章
 * 在两个列表里因为 `equals` 口径不同而闪烁。
 */
class ArticleListAdapter :
    BaseListAdapter<Article, ItemArticleBinding>(
        diffCallback = ArticleDiffCallback(),
        inflate = { inflater, parent -> ItemArticleBinding.inflate(inflater, parent, false) },
    ) {

    override fun onBind(holder: BaseViewHolder<ItemArticleBinding>, item: Article) {
        holder.binding.tvTitle.text = item.title
        holder.binding.tvDesc.text = item.desc
    }
}

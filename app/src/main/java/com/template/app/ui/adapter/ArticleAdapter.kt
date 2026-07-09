package com.template.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.template.app.data.model.Article
import com.template.app.databinding.ItemArticleBinding

/**
 * Paging3 Adapter — 文章列表（HomeFragment 与 ListFragment 共享）
 */
class ArticleAdapter(
    private val onItemClick: ((Article) -> Unit)? = null
) : PagingDataAdapter<Article, ArticleAdapter.VH>(ArticleDiffCallback()) {

    class VH(val binding: ItemArticleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position) ?: return
        holder.binding.tvTitle.text = item.title
        holder.binding.tvDesc.text = item.desc
        holder.binding.root.setOnClickListener {
            val currentItem = getItem(holder.bindingAdapterPosition)
            currentItem?.let { onItemClick?.invoke(it) }
        }
    }
}

class ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(old: Article, new: Article) = old.id == new.id
    override fun areContentsTheSame(old: Article, new: Article) = old == new
}

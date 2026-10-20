package com.template.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.template.app.data.model.Category
import com.template.app.databinding.ItemCategoryBinding
import com.template.core.base.BaseListAdapter

/**
 * 分类列表 Adapter —— [BaseListAdapter] 的标准用法：
 * 只写 onBind，生命周期 / 点击 / DiffUtil 全部由基类处理。
 */
class CategoryAdapter(
    private val onClick: (Category) -> Unit
) : BaseListAdapter<Category, ItemCategoryBinding>(
    diffCallback = object : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Category, newItem: Category) =
            oldItem == newItem
    },
    inflate = { inflater: LayoutInflater, parent: ViewGroup ->
        ItemCategoryBinding.inflate(inflater, parent, false)
    }
) {

    init {
        onItemClick = { _, item -> onClick(item) }
    }

    override fun onBind(holder: BaseViewHolder<ItemCategoryBinding>, item: Category) {
        holder.binding.tvName.text = item.name
        holder.binding.tvId.text = "#${item.id}"
    }
}

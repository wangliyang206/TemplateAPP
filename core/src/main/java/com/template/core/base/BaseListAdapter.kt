package com.template.core.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * 通用 RecyclerView Adapter 基类
 * 使用 ListAdapter + DiffUtil 自动计算差异，避免 notifyDataSetChanged
 *
 * 用法：
 * class UserAdapter : BaseListAdapter<User, ItemUserBinding>(
 *     diffCallback = object : DiffUtil.ItemCallback<User>() {
 *         override fun areItemsTheSame(old: User, new: User) = old.id == new.id
 *         override fun areContentsTheSame(old: User, new: User) = old == new
 *     },
 *     inflate = { inflater, parent -> ItemUserBinding.inflate(inflater, parent, false) }
 * ) {
 *     override fun onBind(holder: BaseViewHolder<ItemUserBinding>, item: User) {
 *         holder.binding.tvName.text = item.name
 *     }
 * }
 */
abstract class BaseListAdapter<T : Any, VB : ViewBinding>(
    diffCallback: DiffUtil.ItemCallback<T>,
    private val inflate: (LayoutInflater, ViewGroup) -> VB,
) : ListAdapter<T, BaseListAdapter.BaseViewHolder<VB>>(diffCallback) {

    /** Item 点击事件 */
    var onItemClick: ((position: Int, item: T) -> Unit)? = null

    /** Item 长按事件 */
    var onItemLongClick: ((position: Int, item: T) -> Boolean)? = null

    /** 绑定数据到 ViewBinding */
    abstract fun onBind(holder: BaseViewHolder<VB>, item: T)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = inflate(inflater, parent)
        val holder = BaseViewHolder(binding)
        binding.root.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onItemClick?.invoke(pos, getItem(pos))
            }
        }
        binding.root.setOnLongClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onItemLongClick?.invoke(pos, getItem(pos)) ?: false
            } else false
        }
        return holder
    }

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        onBind(holder, getItem(position))
    }

    class BaseViewHolder<VB : ViewBinding>(val binding: VB) :
        RecyclerView.ViewHolder(binding.root)
}

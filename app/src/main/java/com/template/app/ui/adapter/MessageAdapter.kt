package com.template.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.template.app.data.model.Message
import com.template.app.databinding.ItemMessageBinding

/**
 * 消息列表 Adapter。
 *
 * 点击整条视为"已读"：未读只影响圆点显隐，不改变条目高度，避免列表跳动。
 */
class MessageAdapter(private val onItemClick: (Message) -> Unit) :
    ListAdapter<Message, MessageAdapter.Holder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    inner class Holder(private val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Message) {
            binding.tvTitle.text = item.title
            binding.tvContent.text = item.content
            binding.tvTime.text = item.time
            // 已读用 INVISIBLE 而不是 GONE：占住位置，否则标题会左移、整行跳动
            binding.dotUnread.visibility = if (item.isRead) View.INVISIBLE else View.VISIBLE
            binding.tvTitle.alpha = if (item.isRead) READ_ALPHA else 1f
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    private companion object {
        const val READ_ALPHA = 0.65f

        val DIFF = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(oldItem: Message, newItem: Message) = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Message, newItem: Message) = oldItem == newItem
        }
    }
}

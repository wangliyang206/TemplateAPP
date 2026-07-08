package com.template.app.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.template.app.data.model.Article
import com.template.app.databinding.FragmentListBinding
import com.template.app.databinding.ItemArticleBinding
import com.template.core.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 列表页 Fragment：RecyclerView + Paging3 + SmartRefreshLayout
 *
 * 展示：
 * 1. Paging3 分页加载
 * 2. 下拉刷新 + 上拉加载更多
 * 3. 空数据/错误状态处理
 */
@AndroidEntryPoint
class ListFragment : BaseFragment<FragmentListBinding>() {

    private val viewModel: ListViewModel by viewModels()

    private lateinit var adapter: ArticleAdapter

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentListBinding.inflate(inflater, container, false)

    override fun initView() {
        adapter = ArticleAdapter(
            onItemClick = { article ->
                // 点击跳转详情
            }
        )
        binding.recyclerView.adapter = adapter

        binding.refreshLayout.setOnRefreshListener {
            adapter.refresh()
        }
    }

    override fun initObserver() {
        lifecycleScope.launch {
            viewModel.articleFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        lifecycleScope.launch {
            adapter.loadStateFlow
                .flowWithLifecycle(lifecycle)
                .collect { loadStates ->
                    binding.refreshLayout.finishRefresh()

                    when (loadStates.refresh) {
                        is LoadState.Loading -> {
                            // 首次加载中
                        }
                        is LoadState.Error -> {
                            val msg = (loadStates.refresh as LoadState.Error).error.message ?: "加载失败"
                            // 显示错误状态
                        }
                        is LoadState.NotLoading -> {
                            // 加载完成
                        }
                    }
                }
        }
    }
}

/**
 * Paging3 Adapter
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

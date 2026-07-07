package com.template.app.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.paging.PagingDataAdapter
import com.template.app.R
import com.template.app.data.model.Article
import com.template.app.databinding.FragmentListBinding
import com.template.app.databinding.ItemArticleBinding
import com.template.core.base.BaseFragment
import com.template.core.base.BaseListAdapter
import dagger.hilt.android.AndroidEntryPoint

/**
 * 列表页 Fragment：RecyclerView + Paging3 + SmartRefreshLayout
 *
 * 展示：
 * 1. Paging3 分页加载
 * 2. BaseListAdapter 通用适配器
 * 3. 下拉刷新 + 上拉加载更多
 * 4. 空数据/错误状态处理
 */
@AndroidEntryPoint
class ListFragment : BaseFragment<FragmentListBinding>() {

    private val viewModel: ListViewModel by viewModels()

    private lateinit var adapter: ArticleAdapter

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentListBinding.inflate(inflater, container, false)

    override fun initView() {
        // 初始化 Adapter
        adapter = ArticleAdapter()
        adapter.onItemClick = { _, article ->
            // 点击跳转详情
        }
        binding.recyclerView.adapter = adapter

        // 下拉刷新
        binding.refreshLayout.setOnRefreshListener {
            adapter.refresh()
        }
    }

    override fun initObserver() {
        // 观察 PagingData
        viewModel.articleFlow.observe(viewLifecycleOwner) { pagingData ->
            adapter.submitData(lifecycle, pagingData)
        }

        // 加载状态监听
        adapter.loadStateFlow.observe(viewLifecycleOwner) { loadStates ->
            binding.refreshLayout.finishRefresh()

            val refreshState = loadStates.refresh
            val appendState = loadStates.append

            when {
                refreshState is androidx.paging.LoadState.Loading -> {
                    // 首次加载中
                }
                refreshState is androidx.paging.LoadState.Error -> {
                    // 首次加载失败
                    val msg = (refreshState as androidx.paging.LoadState.Error).error.message ?: "加载失败"
                    // 显示错误状态
                }
                appendState is androidx.paging.LoadState.Loading -> {
                    // 加载更多中
                }
            }
        }
    }

    override fun initData() {
        // Paging3 自动触发首次加载，无需手动调用
    }
}

/**
 * Paging3 Adapter：继承 PagingDataAdapter
 */
class ArticleAdapter :
    PagingDataAdapter<Article, ArticleAdapter.VH>(ArticleDiffCallback()) {

    class VH(val binding: ItemArticleBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        item?.let {
            holder.binding.tvTitle.text = it.title
            holder.binding.tvDesc.text = it.desc
        }
    }
}

class ArticleDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(old: Article, new: Article) = old.id == new.id
    override fun areContentsTheSame(old: Article, new: Article) = old == new
}

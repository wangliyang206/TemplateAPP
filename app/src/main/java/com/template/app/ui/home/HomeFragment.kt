package com.template.app.ui.home

import android.content.Intent
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
import com.template.app.databinding.FragmentHomeBinding
import com.template.app.databinding.ItemArticleBinding
import com.template.app.ui.webview.WebViewActivity
import com.template.core.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 首页 Fragment：直接展示 Paging3 文章列表
 */
@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var adapter: ArticleAdapter

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHomeBinding.inflate(inflater, container, false)

    override fun initView() {
        adapter = ArticleAdapter(
            onItemClick = { article ->
                val intent = Intent(requireContext(), WebViewActivity::class.java).apply {
                    putExtra("url", article.url)
                    putExtra("title", article.title)
                }
                startActivity(intent)
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
                            // 加载失败
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
 * Paging3 Adapter（与 ListFragment 共享 Article 数据模型）
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

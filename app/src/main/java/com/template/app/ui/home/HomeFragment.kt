package com.template.app.ui.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.template.app.R
import com.template.app.databinding.FragmentHomeBinding
import com.template.app.ui.adapter.ArticleAdapter
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

    /**
     * 返回标题栏 View ID，用于自动适配状态栏高度
     */
    override fun getTitleBarId(): Int = R.id.titleBar

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
        binding.refreshLayout.setOnRefreshListener { adapter.refresh() }
    }

    override fun initObserver() {
        // 收集文章数据，提交给 PagingAdapter
        lifecycleScope.launch {
            viewModel.articleFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        // 监听加载状态，控制下拉刷新和上拉加载的动画结束时机
        lifecycleScope.launch {
            adapter.loadStateFlow.flowWithLifecycle(lifecycle).collect { loadStates ->
                // 处理下拉刷新动画
                when (loadStates.refresh) {
                    is LoadState.Loading -> {
                        // 下拉刷新中，保持动画
                    }
                    is LoadState.Error -> {
                        // 下拉刷新失败，结束动画
                        binding.refreshLayout.finishRefresh()
                    }
                    is LoadState.NotLoading -> {
                        // 下拉刷新完成，结束动画
                        binding.refreshLayout.finishRefresh()
                    }
                }

                // 处理上拉加载更多动画
                when (loadStates.append) {
                    is LoadState.Loading -> {
                        // 上拉加载中，保持动画
                    }
                    is LoadState.Error -> {
                        // 上拉加载失败，结束动画
                        binding.refreshLayout.finishLoadMore()
                    }
                    is LoadState.NotLoading -> {
                        // 上拉加载完成（包括没有更多数据的情况），结束动画
                        binding.refreshLayout.finishLoadMore()
                    }
                }
            }
        }
    }
}
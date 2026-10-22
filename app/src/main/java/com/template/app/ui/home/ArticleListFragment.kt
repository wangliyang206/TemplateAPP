package com.template.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import com.template.app.ui.adapter.ArticleAdapter
import com.template.app.ui.webview.WebViewActivity
import com.template.core.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 文章列表子 Fragment - 按分类展示文章列表
 *
 * 用于首页 ViewPager2 中，每个分类对应一个实例
 */
@AndroidEntryPoint
class ArticleListFragment : BaseFragment<com.template.app.databinding.FragmentArticleListBinding>() {

    companion object {
        private const val ARG_CATEGORY_ID = "category_id"

        /**
         * 创建 Fragment 实例
         * @param categoryId 分类 ID
         */
        fun newInstance(categoryId: Int): ArticleListFragment = ArticleListFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_CATEGORY_ID, categoryId)
            }
        }
    }

    private val viewModel: ArticleListViewModel by viewModels()
    private lateinit var adapter: ArticleAdapter

    /**
     * 当前分类 ID
     */
    private val categoryId: Int by lazy {
        arguments?.getInt(ARG_CATEGORY_ID, 0) ?: 0
    }

    override fun getTitleBarId(): Int = 0

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): com.template.app.databinding.FragmentArticleListBinding =
        com.template.app.databinding.FragmentArticleListBinding.inflate(
            inflater,
            container,
            false,
        )

    override fun initView() {
        // 设置分类 ID，触发数据加载
        viewModel.setCategoryId(categoryId)

        adapter = ArticleAdapter(
            onItemClick = { article ->
                val intent = Intent(requireContext(), WebViewActivity::class.java).apply {
                    putExtra("url", article.url)
                    putExtra("title", article.title)
                }
                startActivity(intent)
            },
        )
        binding.recyclerView.adapter = adapter

        // 下拉刷新
        binding.refreshLayout.setOnRefreshListener {
            adapter.refresh()
        }

        // 启用嵌套滚动，配合 CoordinatorLayout 实现吸顶效果
        binding.recyclerView.isNestedScrollingEnabled = true
    }

    override fun initObserver() {
        // 收集文章数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.articleFlow.collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                }
            }
        }

        // 监听加载状态，控制下拉刷新和上拉加载动画
        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect { loadStates ->
                    // 下拉刷新
                    when (loadStates.refresh) {
                        is LoadState.Loading -> {}
                        is LoadState.Error -> {
                            binding.refreshLayout.finishRefresh()
                        }
                        is LoadState.NotLoading -> {
                            binding.refreshLayout.finishRefresh()
                        }
                    }
                    // 上拉加载更多
                    when (loadStates.append) {
                        is LoadState.Loading -> {}
                        is LoadState.Error -> {
                            binding.refreshLayout.finishLoadMore()
                        }
                        is LoadState.NotLoading -> {
                            binding.refreshLayout.finishLoadMore()
                        }
                    }
                }
        }
    }
}

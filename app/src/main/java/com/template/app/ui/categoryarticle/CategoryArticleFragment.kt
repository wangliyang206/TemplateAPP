package com.template.app.ui.categoryarticle

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.template.app.R
import com.template.app.databinding.FragmentCategoryArticleBinding
import com.template.app.ui.adapter.ArticleAdapter
import com.template.app.ui.articledetail.ArticleDetailActivity
import com.template.app.ui.home.ArticleListViewModel
import com.template.core.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 分类文章页 —— 「从发现页点分类进来」的二级页。
 *
 * 复用 [ArticleListViewModel] 与 [ArticleAdapter]：首页 Tab 与这个页面是同一种列表，
 * 差别只在这里带标题栏和返回入口。**不要为"多一个入口"复制一份列表逻辑**。
 */
@AndroidEntryPoint
class CategoryArticleFragment : BaseFragment<FragmentCategoryArticleBinding>() {

    companion object {
        const val ARG_CATEGORY_ID = "category_id"
        const val ARG_CATEGORY_NAME = "category_name"
    }

    private val viewModel: ArticleListViewModel by viewModels()

    private lateinit var adapter: ArticleAdapter

    private val categoryId: Int by lazy { arguments?.getInt(ARG_CATEGORY_ID, 0) ?: 0 }

    private val categoryName: String by lazy { arguments?.getString(ARG_CATEGORY_NAME).orEmpty() }

    override fun getTitleBarId(): Int = R.id.titleBar

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentCategoryArticleBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.tvTitle.text = categoryName
        binding.ivBack.setOnClickListener { findNavController().navigateUp() }

        viewModel.setCategoryId(categoryId)

        adapter = ArticleAdapter { article ->
            startActivity(ArticleDetailActivity.intent(requireContext(), article.id))
        }
        binding.recyclerView.adapter = adapter

        binding.refreshLayout.setOnRefreshListener { adapter.refresh() }
        binding.recyclerView.isNestedScrollingEnabled = true
    }

    override fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.articleFlow.collectLatest { pagingData -> adapter.submitData(pagingData) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.collect { loadStates ->
                if (loadStates.refresh !is LoadState.Loading) binding.refreshLayout.finishRefresh()
                if (loadStates.append !is LoadState.Loading) binding.refreshLayout.finishLoadMore()
            }
        }
    }
}

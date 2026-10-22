package com.template.app.ui.discover

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.template.app.R
import com.template.app.databinding.FragmentDiscoverBinding
import com.template.app.ui.articledetail.ArticleDetailActivity
import com.template.app.ui.categoryarticle.CategoryArticleFragment
import com.template.core.base.BaseFragment
import com.template.core.ext.collectFlow
import com.template.core.state.UiState
import com.template.core.state.asString
import dagger.hilt.android.AndroidEntryPoint

/**
 * 发现页 —— 「多数据源 + 多 ItemType 列表 + 四态」的参考实现。
 *
 * 两个板块共用一个 RecyclerView（见 [DiscoverAdapter]），点击分类进分类文章页，
 * 点击排行直接开 WebView，与资讯列表的跳转行为保持一致。
 */
@AndroidEntryPoint
class DiscoverFragment : BaseFragment<FragmentDiscoverBinding>() {

    private val viewModel: DiscoverViewModel by viewModels()

    private val adapter by lazy {
        DiscoverAdapter(
            onCategoryClick = { category ->
                findNavController().navigate(
                    R.id.action_discover_to_category_article,
                    bundleOf(
                        CategoryArticleFragment.ARG_CATEGORY_ID to category.id,
                        CategoryArticleFragment.ARG_CATEGORY_NAME to category.name,
                    ),
                )
            },
            onRankClick = { rank -> openArticle(rank.article.id) },
        )
    }

    override fun getTitleBarId(): Int = R.id.titleBar

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentDiscoverBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.recyclerView.adapter = adapter
    }

    override fun initObserver() {
        viewLifecycleOwner.collectFlow(viewModel.state, ::render)
    }

    private fun render(state: UiState<List<DiscoverItem>>) {
        when (state) {
            is UiState.Idle -> Unit
            is UiState.Loading -> binding.stateLayout.showLoading()
            is UiState.Empty -> binding.stateLayout.showEmpty(getString(R.string.category_empty))
            is UiState.Error -> binding.stateLayout.showError(
                message = state.message.asString(requireContext()),
                onRetry = viewModel::load,
            )
            is UiState.Success -> {
                binding.stateLayout.showContent()
                adapter.submitList(state.data)
            }
        }
    }

    private fun openArticle(articleId: Long) {
        startActivity(ArticleDetailActivity.intent(requireContext(), articleId))
    }
}

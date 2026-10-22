package com.template.app.ui.favorite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.template.app.R
import com.template.app.data.model.Article
import com.template.app.databinding.FragmentFavoriteBinding
import com.template.app.ui.adapter.ArticleListAdapter
import com.template.app.ui.articledetail.ArticleDetailActivity
import com.template.core.base.BaseFragment
import com.template.core.ext.collectFlow
import dagger.hilt.android.AndroidEntryPoint

/**
 * 我的收藏 —— 「Room Flow 持续驱动列表」的参考实现。
 *
 * 注意这里没有 `onResume` 重新加载：列表数据来自数据库观察，
 * 在详情页取消收藏返回后会自动更新。
 */
@AndroidEntryPoint
class FavoriteFragment : BaseFragment<FragmentFavoriteBinding>() {

    private val viewModel: FavoriteViewModel by viewModels()

    private val adapter by lazy {
        ArticleListAdapter().apply {
            onItemClick = { _, article ->
                startActivity(ArticleDetailActivity.intent(requireContext(), article.id))
            }
        }
    }

    override fun getTitleBarId(): Int = R.id.titleBar

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentFavoriteBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.ivBack.setOnClickListener { findNavController().navigateUp() }
        binding.recyclerView.adapter = adapter
    }

    override fun initObserver() {
        viewLifecycleOwner.collectFlow(viewModel.articles) { render(it) }
    }

    /**
     * 空态用 StatefulLayout 整块替换内容区，而不是在列表旁边另藏一个 TextView。
     *
     * 这样「有收藏」和「一条不剩」之间来回切换时只需要 `showEmpty()` / `showContent()`，
     * 不用自己维护另一个 View 的可见性 —— 上一版多写的那个 `emptyView`
     * 就是因为想省这一步，结果绑到了布局里根本不存在的控件上。
     */
    private fun render(articles: List<Article>) {
        if (articles.isEmpty()) {
            adapter.submitList(emptyList())
            binding.stateLayout.showEmpty(getString(R.string.favorite_empty))
        } else {
            binding.stateLayout.showContent()
            adapter.submitList(articles)
        }
    }
}

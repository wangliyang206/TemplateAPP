package com.template.app.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.template.app.R
import com.template.app.data.model.Article
import com.template.app.databinding.FragmentHistoryBinding
import com.template.app.ui.adapter.ArticleListAdapter
import com.template.app.ui.articledetail.ArticleDetailActivity
import com.template.core.base.BaseFragment
import com.template.core.ext.collectEffects
import com.template.core.ext.collectFlow
import com.template.core.ext.toastShort
import com.template.core.state.UiEffect
import com.template.core.state.asString
import dagger.hilt.android.AndroidEntryPoint

/**
 * 浏览历史。
 *
 * 三个值得抄的细节：
 * 1. 「清空」是不可逆操作，先弹确认框；确认后只调 `viewModel.clear()`，
 *    列表由 Room Flow 自动变空，不用手动 `adapter.submitList(emptyList())`。
 * 2. 列表为空时「清空」按钮置灰，避免点了没反应。
 * 3. 点击条目进详情页，详情页会再记一次浏览 —— 同主键 REPLACE，只更新时间，不会重复。
 */
@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding>() {

    private val viewModel: HistoryViewModel by viewModels()

    private val adapter by lazy {
        ArticleListAdapter().apply {
            onItemClick = { _, article ->
                openDetail(article)
            }
        }
    }

    override fun getTitleBarId(): Int = R.id.titleBar

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHistoryBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.ivBack.setOnClickListener { findNavController().navigateUp() }
        binding.recyclerView.adapter = adapter
        binding.tvClear.setOnClickListener { confirmClear() }
    }

    override fun initObserver() {
        viewLifecycleOwner.collectFlow(viewModel.articles) { render(it) }
        viewLifecycleOwner.collectEffects(viewModel) { effect ->
            when (effect) {
                is UiEffect.ShowMessage -> requireContext().toastShort(effect.message.asString(requireContext()))
                is UiEffect.Close, is UiEffect.SessionExpired -> Unit
            }
        }
    }

    private fun render(articles: List<Article>) {
        binding.tvClear.isEnabled = articles.isNotEmpty()
        if (articles.isEmpty()) {
            adapter.submitList(emptyList())
            binding.stateLayout.showEmpty(getString(R.string.history_empty))
        } else {
            binding.stateLayout.showContent()
            adapter.submitList(articles)
        }
    }

    private fun confirmClear() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.history_clear_confirm)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.history_clear) { _, _ -> viewModel.clear() }
            .show()
    }

    private fun openDetail(article: Article) {
        startActivity(ArticleDetailActivity.intent(requireContext(), article.id))
    }
}

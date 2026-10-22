package com.template.app.ui.search

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import com.template.app.R
import com.template.app.databinding.FragmentSearchBinding
import com.template.app.ui.adapter.ArticleAdapter
import com.template.app.ui.webview.WebViewActivity
import com.template.core.base.BaseFragment
import com.template.core.ext.collectFlow
import com.template.core.ext.isVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 搜索页 —— 「输入防抖 + Paging3 + 空态」的参考实现。
 *
 * 与列表页的区别只有两点：
 * 1. 数据源是 [SearchViewModel.results]（随关键词变化重建），不是固定的分类流；
 * 2. 空态用页面内的提示文案表达（搜索场景下"还没输入"和"没有结果"是两种不同提示），
 *    所以这里没有套 StatefulLayout 的四态——四态适合"首屏加载一次"的页面。
 */
@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>() {

    private val viewModel: SearchViewModel by viewModels()

    private val adapter by lazy {
        ArticleAdapter { article ->
            startActivity(
                Intent(requireContext(), WebViewActivity::class.java).apply {
                    putExtra("url", article.url)
                    putExtra("title", article.title)
                },
            )
        }
    }

    private var currentKeyword: String = ""
    private var lastPageEmpty: Boolean = false

    override fun getTitleBarId(): Int = R.id.titleBar

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSearchBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.recyclerView.adapter = adapter

        binding.etKeyword.doAfterTextChanged { text ->
            viewModel.onKeywordChange(text?.toString().orEmpty())
        }
        binding.etKeyword.setOnEditorActionListener { _, _, _ ->
            hideKeyboard()
            true
        }

        // Paging 的"加载完了但一条都没有"只能从 LoadState 拿到，adapter 本身不暴露"是否为空"
        adapter.addLoadStateListener { states ->
            val refresh = states.refresh
            lastPageEmpty = refresh is LoadState.NotLoading && adapter.itemCount == 0
            updateHint()
        }
    }

    override fun initObserver() {
        viewLifecycleOwner.collectFlow(viewModel.keyword) { keyword ->
            currentKeyword = keyword
            updateHint()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.results.collectLatest { pagingData -> adapter.submitData(pagingData) }
            }
        }
    }

    /**
     * 三种状态：还没输入 / 没搜到 / 有结果。
     * 注意 `submitData` 是异步的，所以这里只依据"关键词 + 最近一次加载结果"判断，不依赖 itemCount 的时序。
     */
    private fun updateHint() {
        // Paging 的 LoadState 可能在页面销毁后回调，碰 binding 前先判空
        if (view == null) return
        val blank = currentKeyword.isBlank()
        binding.tvHint.isVisible = blank || lastPageEmpty
        binding.tvHint.text = if (blank) getString(R.string.search_idle) else getString(R.string.search_empty)
    }

    private fun hideKeyboard() {
        val manager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        manager?.hideSoftInputFromWindow(binding.etKeyword.windowToken, 0)
    }
}

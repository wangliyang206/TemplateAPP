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

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: ArticleAdapter

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
        lifecycleScope.launch {
            viewModel.articleFlow.collectLatest { pagingData -> adapter.submitData(pagingData) }
        }
        lifecycleScope.launch {
            adapter.loadStateFlow.flowWithLifecycle(lifecycle).collect { loadStates ->
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
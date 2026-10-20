package com.template.app.ui.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.template.app.R
import com.template.app.data.model.Banner
import com.template.app.data.model.Category
import com.template.app.databinding.FragmentHomeBinding
import com.template.app.ui.adapter.BannerImageAdapter
import com.template.app.ui.webview.WebViewActivity
import com.template.core.base.BaseFragment
import com.template.core.ext.collectEffects
import com.template.core.ext.collectFlow
import com.template.core.ext.toastShort
import com.template.core.state.UiEffect
import com.template.core.state.UiState
import com.template.core.state.asString
import com.youth.banner.indicator.CircleIndicator
import dagger.hilt.android.AndroidEntryPoint

/**
 * 首页 Fragment：顶部广告位 + 分类 Tab + 文章列表（吸顶效果）。
 *
 * 状态渲染范式（全项目统一，新页面照抄）：
 *   Idle → 什么都不做
 *   Loading → StatefulLayout.showLoading()
 *   Error → showError(文案) { retry }
 *   Empty → showEmpty()
 *   Success → showContent() + 绑定数据
 */
@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels()
    private var pagerReady = false

    override fun getTitleBarId(): Int = R.id.titleBar

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHomeBinding.inflate(inflater, container, false)

    override fun initObserver() {
        viewLifecycleOwner.collectFlow(viewModel.state, ::render)
        viewLifecycleOwner.collectEffects(viewModel) { effect ->
            when (effect) {
                is UiEffect.ShowMessage ->
                    requireContext().toastShort(effect.message.asString(requireContext()))
                else -> Unit
            }
        }
    }

    private fun render(state: UiState<HomeData>) {
        when (state) {
            is UiState.Idle -> Unit
            is UiState.Loading -> binding.stateLayout.showLoading()
            is UiState.Empty -> binding.stateLayout.showEmpty(getString(R.string.home_empty))
            is UiState.Error -> binding.stateLayout.showError(
                message = state.message.asString(requireContext()),
                onRetry = viewModel::refresh,
            )
            is UiState.Success -> {
                if (state.data.isEmpty) {
                    binding.stateLayout.showEmpty(getString(R.string.home_empty))
                } else {
                    binding.stateLayout.showContent()
                    bind(state.data)
                }
            }
        }
    }

    private fun bind(data: HomeData) {
        setupBanner(data.banners)
        setupViewPager(data.categories)
    }

    private fun setupBanner(banners: List<Banner>) {
        if (banners.isEmpty()) return

        val adapter = BannerImageAdapter(banners).apply {
            onItemClick = { banner ->
                if (banner.linkUrl.isNotEmpty()) {
                    startActivity(
                        Intent(requireContext(), WebViewActivity::class.java).apply {
                            putExtra("url", banner.linkUrl)
                            putExtra("title", banner.title)
                        },
                    )
                }
            }
        }

        binding.banner.apply {
            setAdapter(adapter)
            setIndicator(CircleIndicator(context))
            setLoopTime(BANNER_LOOP_INTERVAL_MS)
            setBannerRound(BANNER_CORNER_RADIUS_DP)
            isAutoLoop(true)
            start()
        }
    }

    /** 分类变化时才重建 ViewPager，避免每次刷新都重置 Tab 位置 */
    private fun setupViewPager(categories: List<Category>) {
        if (pagerReady || categories.isEmpty()) return
        pagerReady = true

        binding.contentViewPager.adapter =
            object : FragmentStateAdapter(childFragmentManager, lifecycle) {
                override fun getItemCount(): Int = categories.size
                override fun createFragment(position: Int): Fragment =
                    ArticleListFragment.newInstance(categories[position].id)
            }

        TabLayoutMediator(binding.tabLayout, binding.contentViewPager) { tab, position ->
            tab.text = categories[position].name
        }.attach()

        binding.contentViewPager.offscreenPageLimit = categories.size.coerceAtMost(MAX_OFFSCREEN_PAGES)
    }

    override fun onResume() {
        super.onResume()
        binding.banner.start()
    }

    override fun onPause() {
        super.onPause()
        binding.banner.stop()
    }

    override fun onDestroyView() {
        binding.banner.destroy()
        // View 销毁但 Fragment 实例还在（底部导航切 Tab 的场景）：
        // pagerReady 必须重置，否则切回时 setupViewPager 直接 return，
        // 新 View 上的 ViewPager 没有 adapter，表现为「切回来首页没数据」
        pagerReady = false
        super.onDestroyView()
    }

    private companion object {
        const val BANNER_LOOP_INTERVAL_MS = 3000L
        const val BANNER_CORNER_RADIUS_DP = 20f

        /** 预加载页数上限：太多会同时持有过多 Fragment 与列表，内存换不来收益 */
        const val MAX_OFFSCREEN_PAGES = 4
    }
}

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
import com.youth.banner.indicator.CircleIndicator
import dagger.hilt.android.AndroidEntryPoint

/**
 * 首页 Fragment：顶部广告位 + 分类 Tab + 文章列表（吸顶效果）
 */
@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels()

    override fun getTitleBarId(): Int = R.id.titleBar

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHomeBinding.inflate(inflater, container, false)

    override fun initView() {}

    override fun initObserver() {
        // 观察轮播图数据
        viewModel.bannerList.observe(viewLifecycleOwner) { banners ->
            setupBanner(banners)
        }

        // 观察分类数据
        viewModel.categoryList.observe(viewLifecycleOwner) { categories ->
            setupViewPager(categories)
        }
    }

    override fun initData() {
        // 加载首页数据
        viewModel.loadHomeData()
    }

    /**
     * 设置轮播图 - 使用 youth5201314 Banner 库
     */
    private fun setupBanner(banners: List<Banner>) {
        if (banners.isEmpty()) return

        val adapter = BannerImageAdapter(banners).apply {
            onItemClick = { banner ->
                if (banner.linkUrl.isNotEmpty()) {
                    val intent = Intent(requireContext(), WebViewActivity::class.java).apply {
                        putExtra("url", banner.linkUrl)
                        putExtra("title", banner.title)
                    }
                    startActivity(intent)
                }
            }
        }

        binding.banner.apply {
            // 设置适配器
            setAdapter(adapter)
            // 设置圆形指示器
            setIndicator(CircleIndicator(context))
            // 设置轮播间隔 3 秒
            setLoopTime(3000)
            // 设置圆角
            setBannerRound(20f)
            // 开启自动轮播并启动
            isAutoLoop(true)
            start()
        }
    }

    /**
     * 设置 ViewPager2 和 TabLayout
     */
    private fun setupViewPager(categories: List<Category>) {
        binding.contentViewPager.adapter = object : FragmentStateAdapter(childFragmentManager, lifecycle) {
            override fun getItemCount(): Int = categories.size

            override fun createFragment(position: Int): Fragment {
                return ArticleListFragment.newInstance(categories[position].id)
            }
        }

        // TabLayout 与 ViewPager2 联动
        TabLayoutMediator(binding.tabLayout, binding.contentViewPager) { tab, position ->
            tab.text = categories[position].name
        }.attach()

        // 预加载数量，避免频繁重建
        binding.contentViewPager.offscreenPageLimit = categories.size.coerceAtMost(4)
    }

    override fun onResume() {
        super.onResume()
        // 恢复轮播
        binding.banner.start()
    }

    override fun onPause() {
        super.onPause()
        // 暂停轮播
        binding.banner.stop()
    }

    override fun onDestroyView() {
        // 先销毁轮播（此时 binding 还未置空），再调用父类方法
        binding.banner.destroy()
        super.onDestroyView()
    }
}
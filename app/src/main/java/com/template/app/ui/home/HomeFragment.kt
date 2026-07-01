package com.template.app.ui.home

import androidx.fragment.app.viewModels
import com.template.app.databinding.FragmentHomeBinding
import com.template.core.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels()

    override fun initView() {
        binding.tvTitle.text = "首页"
    }

    override fun initObserver() {
    }

    override fun initData() {
        viewModel.loadData()
    }
}

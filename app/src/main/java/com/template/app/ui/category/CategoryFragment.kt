package com.template.app.ui.category

import androidx.fragment.app.viewModels
import com.template.app.databinding.FragmentCategoryBinding
import com.template.core.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryFragment : BaseFragment<FragmentCategoryBinding>() {

    private val viewModel: CategoryViewModel by viewModels()

    override fun initView() {
        binding.tvTitle.text = "分类"
    }

    override fun initObserver() {
    }

    override fun initData() {
        viewModel.loadData()
    }
}

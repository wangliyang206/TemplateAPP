package com.template.app.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.template.app.databinding.FragmentCategoryBinding
import com.template.core.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryFragment : BaseFragment<FragmentCategoryBinding>() {

    private val viewModel: CategoryViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentCategoryBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.tvTitle.text = "分类"
    }

    override fun initObserver() {}

    override fun initData() {
        viewModel.loadData()
    }
}

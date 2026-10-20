package com.template.app.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.template.app.R
import com.template.app.databinding.FragmentCategoryBinding
import com.template.app.ui.adapter.CategoryAdapter
import com.template.core.base.BaseFragment
import com.template.core.ext.collectFlow
import com.template.core.ext.toastShort
import com.template.core.state.UiState
import com.template.core.state.asString
import dagger.hilt.android.AndroidEntryPoint

/**
 * 分类页 —— 「列表 + 四态 + 通用 Adapter」的参考实现。
 * 新建列表页可以直接照抄这个文件 + CategoryViewModel + CategoryRepository。
 */
@AndroidEntryPoint
class CategoryFragment : BaseFragment<FragmentCategoryBinding>() {

    private val viewModel: CategoryViewModel by viewModels()
    private val adapter by lazy {
        CategoryAdapter { category ->
            requireContext().toastShort(category.name)
        }
    }

    override fun getTitleBarId(): Int = R.id.titleBar

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentCategoryBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.recyclerView.adapter = adapter
    }

    override fun initObserver() {
        viewLifecycleOwner.collectFlow(viewModel.state, ::render)
    }

    private fun render(state: UiState<List<com.template.app.data.model.Category>>) {
        when (state) {
            is UiState.Idle -> Unit
            is UiState.Loading -> binding.stateLayout.showLoading()
            is UiState.Empty -> binding.stateLayout.showEmpty(getString(R.string.category_empty))
            is UiState.Error -> binding.stateLayout.showError(
                message = state.message.asString(requireContext()),
                onRetry = viewModel::load,
            )
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    binding.stateLayout.showEmpty(getString(R.string.category_empty))
                } else {
                    binding.stateLayout.showContent()
                    adapter.submitList(state.data)
                }
            }
        }
    }
}

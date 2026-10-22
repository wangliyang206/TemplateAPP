package com.template.app.ui.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.template.app.R
import com.template.app.data.model.Message
import com.template.app.databinding.FragmentMessageBinding
import com.template.app.ui.adapter.MessageAdapter
import com.template.core.base.BaseFragment
import com.template.core.ext.collectFlow
import com.template.core.ext.toastShort
import com.template.core.state.UiState
import com.template.core.state.asString
import dagger.hilt.android.AndroidEntryPoint

/**
 * 消息页 —— 「单例状态源 + 列表」的参考实现。
 *
 * 未读角标由 MainActivity 直接观察 [com.template.app.data.message.MessageCenter]，
 * 所以这里标记已读后，底部导航的角标会跟着变。
 */
@AndroidEntryPoint
class MessageFragment : BaseFragment<FragmentMessageBinding>() {

    private val viewModel: MessageViewModel by viewModels()

    private val adapter by lazy {
        MessageAdapter { message ->
            viewModel.markRead(message)
            requireContext().toastShort(message.title)
        }
    }

    override fun getTitleBarId(): Int = R.id.titleBar

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentMessageBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.recyclerView.adapter = adapter
        binding.tvMarkAllRead.setOnClickListener { viewModel.markAllRead() }
    }

    override fun initObserver() {
        viewLifecycleOwner.collectFlow(viewModel.state, ::render)
    }

    private fun render(state: UiState<List<Message>>) {
        when (state) {
            is UiState.Idle -> Unit
            is UiState.Loading -> binding.stateLayout.showLoading()
            is UiState.Empty -> binding.stateLayout.showEmpty(getString(R.string.message_empty))
            is UiState.Error -> binding.stateLayout.showError(
                message = state.message.asString(requireContext()),
                onRetry = viewModel::load,
            )
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    binding.stateLayout.showEmpty(getString(R.string.message_empty))
                } else {
                    binding.stateLayout.showContent()
                    adapter.submitList(state.data)
                }
            }
        }
    }
}

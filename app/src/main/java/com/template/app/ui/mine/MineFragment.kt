package com.template.app.ui.mine

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.template.app.R
import com.template.app.databinding.FragmentMineBinding
import com.template.app.ui.login.LoginActivity
import com.template.core.base.BaseFragment
import com.template.core.ext.loadCircleOrDrawable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MineFragment : BaseFragment<FragmentMineBinding>() {

    private val viewModel: MineViewModel by viewModels()

    override fun getTitleBarId(): Int = R.id.titleBar

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentMineBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.layoutUser.setOnClickListener {
            if (viewModel.userState.value is MineViewModel.UserUiState.NotLoggedIn) {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
        }
        binding.btnLogout.setOnClickListener { viewModel.logout() }
    }

    override fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userState.collect { state -> renderUserState(state) }
            }
        }
    }

    private fun renderUserState(state: MineViewModel.UserUiState) {
        when (state) {
            is MineViewModel.UserUiState.NotLoggedIn -> {
                binding.ivAvatar.setImageResource(R.drawable.bg_avatar_circle)
                binding.tvNickname.text = getString(R.string.mine_not_login)
                binding.tvPhone.text = getString(R.string.mine_click_to_login)
                binding.btnLogout.visibility = android.view.View.GONE
            }

            is MineViewModel.UserUiState.LoggedIn -> {
                binding.ivAvatar.loadCircleOrDrawable(state.avatar, R.drawable.bg_avatar_circle)
                binding.tvNickname.text = state.userName.ifEmpty { "用户" }
                binding.tvPhone.text = state.phone
                binding.btnLogout.visibility = android.view.View.VISIBLE
            }
        }
    }
}

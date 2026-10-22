package com.template.app.ui.mine

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.template.app.R
import com.template.app.databinding.FragmentMineBinding
import com.template.app.ui.login.LoginActivity
import com.template.core.base.BaseFragment
import com.template.core.ext.loadCircleOrDrawable
import com.template.core.ext.pickImage
import com.template.core.ext.registerPhotoPicker
import com.template.core.ext.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MineFragment : BaseFragment<FragmentMineBinding>() {

    private val viewModel: MineViewModel by viewModels()

    /**
     * 图片选择：PhotoPicker 不需要任何存储权限，选完只拿到一个临时 Uri。
     * 要长期保存请把流复制到 App 私有目录（见 core 的 registerPhotoPicker 注释）。
     */
    private val pickPhoto = registerPhotoPicker { uri ->
        // 回调可能在 View 已销毁后到达（用户在相册停留期间被系统回收），先判空再碰 binding
        if (view != null) {
            binding.tvPhotoStatus.text = uri?.let { getString(R.string.mine_photo_picked, it.toString()) }.orEmpty()
        }
    }

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
        // 文件上传示例入口，完整链路见 UploadRepository / MineViewModel.uploadDemo
        binding.btnUploadDemo.setOnClickListener { viewModel.uploadDemo() }
        // 图片选择示例（系统 PhotoPicker）
        binding.btnPickPhoto.setOnSingleClickListener { pickPhoto.pickImage() }
        // 设置页入口（DataStore 业务键示例）
        binding.btnSettings.setOnSingleClickListener {
            findNavController().navigate(R.id.action_mine_to_settings)
        }
    }

    override fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userState.collect { state -> renderUserState(state) }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uploadState.collect { state -> renderUploadState(state) }
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

    private fun renderUploadState(state: MineViewModel.UploadUiState) {
        binding.btnUploadDemo.isEnabled = state !is MineViewModel.UploadUiState.Uploading
        binding.tvUploadStatus.text = when (state) {
            is MineViewModel.UploadUiState.Idle -> ""
            is MineViewModel.UploadUiState.Uploading -> getString(R.string.mine_upload_progress, state.progress)
            is MineViewModel.UploadUiState.Done -> getString(R.string.mine_upload_done, state.serverBody)
            is MineViewModel.UploadUiState.Failed -> getString(R.string.mine_upload_failed, state.message)
        }
    }
}

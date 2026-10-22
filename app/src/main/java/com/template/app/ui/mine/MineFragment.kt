package com.template.app.ui.mine

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.template.app.R
import com.template.app.databinding.FragmentMineBinding
import com.template.app.ui.login.LoginActivity
import com.template.core.base.BaseFragment
import com.template.core.ext.collectFlow
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
        bindEntries()
        binding.layoutUser.setOnClickListener {
            if (viewModel.userState.value is MineViewModel.UserUiState.NotLoggedIn) {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
        }
        binding.btnLogout.setOnClickListener { viewModel.logout() }
    }

    /**
     * 5 个条目共用 `layout_mine_row.xml`，标题在这里按资源 id 填。
     *
     * 为什么不写死在 include 里：同一个行布局复用 5 次，写死就没法改了；
     * 而用 `setText(R.string.xxx)` 而不是字面量，切到英文环境会自动跟着变
     * （直接写字符串还会触发 lint 的 SetTextI18n）。
     */
    private fun bindEntries() {
        binding.rowFavorite.tvTitle.setText(R.string.mine_favorite)
        binding.rowHistory.tvTitle.setText(R.string.mine_history)
        binding.rowPickPhoto.tvTitle.setText(R.string.mine_pick_photo)
        binding.rowUploadDemo.tvTitle.setText(R.string.mine_upload_demo)
        binding.rowSettings.tvTitle.setText(R.string.mine_settings)

        // 二级页统一用 navigate(actionId)：转场动画声明在导航图里，调用处不用重复写
        binding.rowFavorite.root.setOnSingleClickListener {
            findNavController().navigate(R.id.action_mine_to_favorite)
        }
        binding.rowHistory.root.setOnSingleClickListener {
            findNavController().navigate(R.id.action_mine_to_history)
        }
        binding.rowSettings.root.setOnSingleClickListener {
            findNavController().navigate(R.id.action_mine_to_settings)
        }
        // 文件上传示例入口，完整链路见 UploadRepository / MineViewModel.uploadDemo
        binding.rowUploadDemo.root.setOnClickListener { viewModel.uploadDemo() }
        // 图片选择示例（系统 PhotoPicker）
        binding.rowPickPhoto.root.setOnSingleClickListener { pickPhoto.pickImage() }
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
        // 条数来自 Room 的 COUNT(*) Flow：收藏或看完文章返回时数字已经是最新的
        viewLifecycleOwner.collectFlow(viewModel.favoriteCount) { renderCount(binding.rowFavorite.tvValue, it) }
        viewLifecycleOwner.collectFlow(viewModel.historyCount) { renderCount(binding.rowHistory.tvValue, it) }
    }

    private fun renderUserState(state: MineViewModel.UserUiState) {
        when (state) {
            is MineViewModel.UserUiState.NotLoggedIn -> {
                binding.ivAvatar.setImageResource(R.drawable.bg_avatar_circle)
                binding.tvNickname.text = getString(R.string.mine_not_login)
                binding.tvPhone.text = getString(R.string.mine_click_to_login)
                binding.btnLogout.visibility = View.GONE
            }

            is MineViewModel.UserUiState.LoggedIn -> {
                binding.ivAvatar.loadCircleOrDrawable(state.avatar, R.drawable.bg_avatar_circle)
                binding.tvNickname.text = state.userName.ifEmpty { "用户" }
                binding.tvPhone.text = state.phone
                binding.btnLogout.visibility = View.VISIBLE
            }
        }
    }

    private fun renderUploadState(state: MineViewModel.UploadUiState) {
        binding.rowUploadDemo.root.isEnabled = state !is MineViewModel.UploadUiState.Uploading
        binding.tvUploadStatus.text = when (state) {
            is MineViewModel.UploadUiState.Idle -> ""
            is MineViewModel.UploadUiState.Uploading -> getString(R.string.mine_upload_progress, state.progress)
            is MineViewModel.UploadUiState.Done -> getString(R.string.mine_upload_done, state.serverBody)
            is MineViewModel.UploadUiState.Failed -> getString(R.string.mine_upload_failed, state.message)
        }
    }

    /** 数量为 0 时不显示「0」，右侧留白比一个孤零零的零更干净 */
    private fun renderCount(target: TextView, count: Int) {
        target.text = if (count > 0) getString(R.string.mine_count_format, count) else ""
    }
}

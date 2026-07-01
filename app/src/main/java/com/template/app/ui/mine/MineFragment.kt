package com.template.app.ui.mine

import android.content.Intent
import androidx.fragment.app.viewModels
import com.template.app.databinding.FragmentMineBinding
import com.template.app.ui.login.LoginActivity
import com.template.core.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MineFragment : BaseFragment<FragmentMineBinding>() {

    private val viewModel: MineViewModel by viewModels()

    override fun initView() {
        binding.tvTitle.text = "我的"
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun initObserver() {
    }

    override fun initData() {
    }
}

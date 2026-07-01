package com.template.app.ui.register

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.template.app.databinding.ActivityRegisterBinding
import com.template.core.base.BaseActivity
import com.template.core.ext.toastShort
import com.template.core.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity : BaseActivity<ActivityRegisterBinding>() {

    private val viewModel: RegisterViewModel by viewModels()

    override fun initView() {
        binding.btnRegister.setOnClickListener { register() }
    }

    override fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerState.collect {
                    when (it) {
                        is UiState.Loading -> { }
                        is UiState.Success -> {
                            toastShort("注册成功")
                            finish()
                        }
                        is UiState.Error -> toastShort(it.message)
                    }
                }
            }
        }
    }

    private fun register() {
        val phone = binding.etPhone.text.toString().trim()
        val pwd = binding.etPassword.text.toString().trim()
        val confirmPwd = binding.etConfirmPassword.text.toString().trim()
        viewModel.register(phone, pwd, confirmPwd)
    }
}

package com.template.app.ui.register

import android.view.LayoutInflater
import androidx.activity.viewModels
import com.template.app.R
import com.template.app.databinding.ActivityRegisterBinding
import com.template.core.base.BaseActivity
import com.template.core.ext.collectFlow
import com.template.core.ext.toastShort
import com.template.core.state.UiState
import com.template.core.state.asString
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : BaseActivity<ActivityRegisterBinding>() {

    private val viewModel: RegisterViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater) = ActivityRegisterBinding.inflate(inflater)

    override fun initView() {
        binding.btnRegister.setOnClickListener { register() }
    }

    override fun initObserver() {
        collectFlow(viewModel.registerState) { state ->
            when (state) {
                is UiState.Loading -> binding.btnRegister.isEnabled = false
                is UiState.Success -> {
                    binding.btnRegister.isEnabled = true
                    toastShort(getString(R.string.register_success))
                    finish()
                }
                is UiState.Error -> {
                    binding.btnRegister.isEnabled = true
                    toastShort(state.message.asString(this@RegisterActivity))
                }
                else -> Unit
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

package com.template.app.ui.login

import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.template.app.MainActivity
import com.template.app.R
import com.template.app.databinding.ActivityLoginBinding
import com.template.app.ui.register.RegisterActivity
import com.template.core.base.BaseActivity
import com.template.core.ext.collectFlow
import com.template.core.ext.isPhone
import com.template.core.ext.toastShort
import com.template.core.state.UiState
import com.template.core.state.asString
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater) = ActivityLoginBinding.inflate(inflater)

    override fun initView() {
        binding.btnLogin.setOnClickListener { login() }
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun initObserver() {
        collectFlow(viewModel.loginState) { state ->
            when (state) {
                is UiState.Idle -> binding.btnLogin.isEnabled = true
                is UiState.Loading -> binding.btnLogin.isEnabled = false
                is UiState.Success -> {
                    binding.btnLogin.isEnabled = true
                    toastShort(getString(R.string.login_success))
                    startActivity(
                        Intent(this@LoginActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        },
                    )
                    finish()
                }
                is UiState.Error -> {
                    binding.btnLogin.isEnabled = true
                    toastShort(state.message.asString(this@LoginActivity))
                }
                else -> Unit
            }
        }
    }

    private fun login() {
        val phone = binding.etPhone.text.toString().trim()
        val pwd = binding.etPassword.text.toString().trim()

        when {
            phone.isEmpty() -> toastShort(getString(R.string.login_phone_required))
            !phone.isPhone() -> toastShort(getString(R.string.login_phone_invalid))
            pwd.isEmpty() -> toastShort(getString(R.string.login_password_required))
            pwd.length < MIN_PASSWORD_LENGTH ->
                toastShort(getString(R.string.login_password_too_short, MIN_PASSWORD_LENGTH))
            else -> viewModel.login(phone, pwd)
        }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}

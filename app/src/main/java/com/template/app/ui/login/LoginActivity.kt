package com.template.app.ui.login

import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.template.app.MainActivity
import com.template.app.databinding.ActivityLoginBinding
import com.template.core.base.BaseActivity
import com.template.core.ext.toastShort
import com.template.core.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater) = ActivityLoginBinding.inflate(inflater)

    override fun initView() {
        binding.btnLogin.setOnClickListener { login() }
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, com.template.app.ui.register.RegisterActivity::class.java))
        }
    }

    override fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect {
                    when (it) {
                        is UiState.Loading -> { }
                        is UiState.Success -> {
                            toastShort("登录成功")
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                        is UiState.Error -> toastShort(it.message)
                    }
                }
            }
        }
    }

    private fun login() {
        val phone = binding.etPhone.text.toString().trim()
        val pwd = binding.etPassword.text.toString().trim()

        // 手机号必填 + 格式校验（11 位数字，1 开头）
        if (phone.isEmpty()) {
            toastShort("请输入手机号")
            return
        }
        if (!phone.matches(Regex("^1[0-9]{10}$"))) {
            toastShort("请输入正确的手机号")
            return
        }

        // 密码必填 + 长度校验（至少 6 位）
        if (pwd.isEmpty()) {
            toastShort("请输入密码")
            return
        }
        if (pwd.length < 6) {
            toastShort("密码长度不能少于6位")
            return
        }

        viewModel.login(phone, pwd)
    }
}

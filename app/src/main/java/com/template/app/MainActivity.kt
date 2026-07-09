package com.template.app

import android.os.Bundle
import android.view.LayoutInflater
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.template.app.databinding.ActivityMainBinding
import com.template.core.base.BaseActivity
import com.template.core.network.DeviceInfoCollector
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun inflateBinding(inflater: LayoutInflater) = ActivityMainBinding.inflate(inflater)

    override fun initView() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)

        // 如果已有手机权限，刷新 cell/simid 信息
        if (DeviceInfoCollector.hasPhonePermission(this)) {
            DeviceInfoCollector.refreshPhoneInfo(this)
        }
    }
}

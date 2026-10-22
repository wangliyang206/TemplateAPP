package com.template.app

import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.template.app.databinding.ActivityMainBinding
import com.template.core.base.BaseActivity
import com.template.core.ext.isVisible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun inflateBinding(inflater: LayoutInflater) = ActivityMainBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        // 必须在 super.onCreate 之前调用：接管系统的启动屏，
        // 首帧绘制完成后自动消失（不需要手动 sleep/delay）。
        installSplashScreen()
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)

        // 二级页（搜索 / 设置）不属于底部导航，进入时把底栏收起来：
        // 否则用户会在详情类页面看到"首页/分类/我的"三个 Tab，误以为可以直接切走。
        val topLevelIds = setOf(R.id.homeFragment, R.id.categoryFragment, R.id.mineFragment)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.isVisible = destination.id in topLevelIds
        }
    }
}

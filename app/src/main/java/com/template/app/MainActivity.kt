package com.template.app

import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.template.app.data.message.MessageCenter
import com.template.app.databinding.ActivityMainBinding
import com.template.core.base.BaseActivity
import com.template.core.ext.isVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    /** 未读角标的数据源在单例里，Activity 直接观察，不需要经过 Fragment */
    @Inject
    lateinit var messageCenter: MessageCenter

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

        // 二级页（搜索 / 设置 / 分类文章）不属于底部导航，进入时把底栏收起来：
        // 否则用户会在详情类页面看到四个 Tab，误以为可以直接切走。
        val topLevelIds = setOf(
            R.id.homeFragment,
            R.id.discoverFragment,
            R.id.messageFragment,
            R.id.mineFragment,
        )
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.isVisible = destination.id in topLevelIds
        }

        observeUnreadBadge()
    }

    /**
     * 未读角标：0 条时移除，避免"0"这种没意义的数字挂在图标上。
     * `BadgeDrawable` 默认最多显示 4 位数字，超出会截断，不需要额外处理。
     */
    private fun observeUnreadBadge() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                messageCenter.unreadCount.collect { count ->
                    if (count <= 0) {
                        binding.bottomNav.removeBadge(R.id.messageFragment)
                    } else {
                        binding.bottomNav.getOrCreateBadge(R.id.messageFragment).apply {
                            number = count
                            isVisible = true
                        }
                    }
                }
            }
        }
    }
}

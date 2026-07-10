package com.template.core.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.gyf.immersionbar.ImmersionBar

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    // 延迟初始化的 binding 实例，子类可直接访问
    lateinit var binding: VB
        private set

    abstract fun inflateBinding(inflater: LayoutInflater): VB

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 调用传入的 inflate 函数创建绑定
        binding = inflateBinding(layoutInflater)
        // 设置内容视图
        setContentView(binding.root)

        // 初始化状态栏
        initStatusBar()
        // 后续钩子方法
        initView()
        initObserver()
        initData()
    }

    /**
     * 初始化状态栏配置，子类可覆写以下方法自定义：
     * - [getTitleBarId] 返回标题栏 ID，自动适配状态栏高度
     * - [isStatusBarDarkFont] 是否深色字体
     * - [getStatusBarColor] 状态栏颜色（0 表示透明）
     * - [isNavigationBarEnable] 是否显示导航栏
     */
    protected open fun initStatusBar() {
        val immersionBar = ImmersionBar.with(this)
            .statusBarDarkFont(isStatusBarDarkFont())

        val titleBarId = getTitleBarId()
        if (titleBarId != 0) {
            immersionBar.titleBar(titleBarId)
        }

        val statusBarColor = getStatusBarColor()
        if (statusBarColor != 0) {
            immersionBar.statusBarColor(statusBarColor)
        }

        if (!isNavigationBarEnable()) {
            immersionBar.hideBar(ImmersionBar.NAVIGATION_BAR)
        }

        immersionBar.init()
    }

    /**
     * 返回标题栏 View ID，用于自动适配状态栏高度
     * 返回 0 表示不需要自动适配
     */
    protected open fun getTitleBarId(): Int = 0

    /**
     * 是否使用深色状态栏字体（白底黑字），默认 true
     */
    protected open fun isStatusBarDarkFont(): Boolean = true

    /**
     * 状态栏颜色，返回 0 表示透明状态栏
     */
    protected open fun getStatusBarColor(): Int = 0

    /**
     * 是否显示底部导航栏，默认 true
     */
    protected open fun isNavigationBarEnable(): Boolean = true

    /**
     * 初始化视图，子类重写此方法进行 findViewById 或 ViewBinding 操作。
     */
    protected open fun initView() {}

    /**
     * 初始化观察者（如 LiveData 订阅）。
     */
    protected open fun initObserver() {}

    /**
     * 初始化数据（如加载网络数据）。
     */
    protected open fun initData() {}
}
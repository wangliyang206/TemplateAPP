package com.template.core.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.CallSuper
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
     * 状态栏样式定制，默认深色字体。子类可覆写 [isStatusBarDarkFont] 调整。
     */
    protected open fun initStatusBar() {
        ImmersionBar.with(this)
            .statusBarDarkFont(isStatusBarDarkFont())
            .init()
    }

    /**
     * 是否使用深色状态栏字体（白底黑字），默认 true。
     * 若为 false 则使用浅色字体（黑底白字）。
     */
    protected open fun isStatusBarDarkFont(): Boolean = true
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

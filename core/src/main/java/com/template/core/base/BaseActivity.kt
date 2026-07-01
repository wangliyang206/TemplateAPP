package com.template.core.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.gyf.immersionbar.ImmersionBar
import java.lang.reflect.ParameterizedType

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    lateinit var binding: VB
        private set

    @Suppress("UNCHECKED_CAST")
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val type = javaClass.genericSuperclass as ParameterizedType
        val clazz = type.actualTypeArguments[0] as Class<VB>
        val method = clazz.getMethod("inflate", LayoutInflater::class.java)
        binding = method.invoke(null, layoutInflater) as VB
        setContentView(binding.root)
        initStatusBar()
        initView()
        initObserver()
        initData()
    }

    protected open fun initStatusBar() {
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .init()
    }

    protected open fun initView() {}

    protected open fun initObserver() {}

    protected open fun initData() {}
}

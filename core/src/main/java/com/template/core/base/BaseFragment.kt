package com.template.core.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.gyf.immersionbar.ImmersionBar

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null
    val binding: VB get() = _binding!!

    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    protected abstract fun getTitleBarId(): Int

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateBinding(inflater, container)
        return _binding!!.root
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initStatusBar()
        initView()
        initObserver()
        initData()
    }

    private fun initStatusBar() {
        val id = getTitleBarId()
        if (id != 0 && isAdded && activity != null) {
            ImmersionBar.with(this)
                .statusBarDarkFont(true)
                .titleBar(id)
                .init()
        }
    }

    protected open fun initView() {}

    protected open fun initObserver() {}

    protected open fun initData() {}

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

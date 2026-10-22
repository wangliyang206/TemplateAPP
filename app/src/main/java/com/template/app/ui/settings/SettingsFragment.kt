package com.template.app.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.template.app.R
import com.template.app.data.settings.AppSettings
import com.template.app.databinding.FragmentSettingsBinding
import com.template.core.base.BaseFragment
import com.template.core.ext.collectFlow
import dagger.hilt.android.AndroidEntryPoint

/**
 * 设置页 —— 「DataStore 业务键 + 开关即时生效」的参考实现。
 *
 * 页面只做两件事：把 [SettingsViewModel.settings] 渲染成开关状态、把开关回调转给 ViewModel。
 * 不做本地缓存（真相源是 DataStore），改完立刻回流。
 *
 * **防回环**：`SwitchMaterial.setChecked()` 也会触发 `OnCheckedChangeListener`，
 * 如果不加 [isRendering] 开关，就会出现"回填状态 → 当成用户操作 → 再写一次库"的自激循环。
 */
@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    private val viewModel: SettingsViewModel by viewModels()

    /** 正在用代码回填状态，期间忽略监听回调 */
    private var isRendering = false

    override fun getTitleBarId(): Int = R.id.titleBar

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSettingsBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.switchWifiOnly.setOnCheckedChangeListener { _, checked ->
            if (!isRendering) viewModel.setWifiOnlyDownload(checked)
        }
        binding.switchPush.setOnCheckedChangeListener { _, checked ->
            if (!isRendering) viewModel.setPushEnabled(checked)
        }
    }

    override fun initObserver() {
        viewLifecycleOwner.collectFlow(viewModel.settings, ::render)
    }

    private fun render(settings: AppSettings) {
        isRendering = true
        binding.switchWifiOnly.isChecked = settings.wifiOnlyDownload
        binding.switchPush.isChecked = settings.pushEnabled
        isRendering = false
    }
}

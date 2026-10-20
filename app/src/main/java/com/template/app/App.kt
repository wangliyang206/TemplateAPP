package com.template.app

import android.content.Intent
import android.os.Build
import com.template.app.ui.login.LoginActivity
import com.template.core.BaseApplication
import com.template.core.network.DeviceInfoCollector
import com.template.core.network.NetworkConfig
import com.template.core.network.SessionManager
import com.template.core.network.SignUtils
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class App : BaseApplication() {

    @Inject
    lateinit var sessionManager: SessionManager

    /** Application 作用域，跟随进程生命周期 */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()

        initNetworkConfig()
        observeSession()
    }

    private fun initNetworkConfig() {
        NetworkConfig.deviceOs = "Android ${Build.VERSION.RELEASE}"
        NetworkConfig.channel = "official"
        NetworkConfig.language = Locale.getDefault().language.uppercase()

        // 与 IS_DEBUG_DATA 联动：mock 构建下「上传示例」端点没有真实后端，
        // 由 MockUploadInterceptor 短路返回伪 200，示例才能跑通成功分支
        NetworkConfig.mockUploadEnabled = BuildConfig.IS_DEBUG_DATA

        // 语义上的兜底：debug 用本地占位密钥，release 必须由构建注入
        NetworkConfig.signSecret = BuildConfig.SIGN_SECRET
        SignUtils.init(NetworkConfig.signSecret)
        if (BuildConfig.SIGN_SECRET.isBlank() && !BuildConfig.DEBUG) {
            Timber.w("SIGN_SECRET 未配置，请求签名将为可预测的空值，请在 CI 注入")
        }

        DeviceInfoCollector.init(this)
    }

    /**
     * 全局登录态失效处理：401 且刷新失败时由 TokenAuthenticator 触发。
     * 业务页面无需关心，也不会出现「每个页面各写一套登出」。
     */
    private fun observeSession() {
        appScope.launch {
            sessionManager.expired.collect {
                Timber.w("登录态失效，跳转登录页")
                startActivity(
                    Intent(this@App, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    },
                )
            }
        }
    }
}

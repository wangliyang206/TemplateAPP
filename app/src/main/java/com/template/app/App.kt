package com.template.app

import android.os.Build
import com.template.core.BaseApplication
import com.template.core.network.DeviceInfoCollector
import com.template.core.network.NetworkConfig
import com.template.core.network.RetrofitClient
import com.template.core.network.SignUtils
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@HiltAndroidApp
class App : BaseApplication() {

    override fun onCreate() {
        super.onCreate()

        // 配置统一请求头
        NetworkConfig.deviceOs = "Android ${Build.VERSION.RELEASE}"
        NetworkConfig.channel = "official"
        NetworkConfig.language = Locale.getDefault().language.uppercase()

        // 采集设备信息（无权限的 cell/simid 自动传空字符串）
        DeviceInfoCollector.init(this)

        // 配置签名密钥（生产环境应从服务端获取或写入 local.properties / BuildConfig）
        NetworkConfig.signSecret = "template_app_sign_secret"
        SignUtils.init(NetworkConfig.signSecret)

        // 初始化网络层
        RetrofitClient.init(BuildConfig.API_HOST)
    }
}

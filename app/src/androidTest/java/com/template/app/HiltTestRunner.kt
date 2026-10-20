package com.template.app

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * 仪器化测试专用 Runner。
 *
 * 为什么需要它：生产 `App` 是 `@HiltAndroidTest` 之外的真实 Application，
 * 直接跑会因为没有 Hilt 测试组件而注入失败。
 * 换成 [HiltTestApplication] 后，`@AndroidEntryPoint` 的页面可以在测试里正常启动。
 *
 * 注意：必须在 `defaultConfig.testInstrumentationRunner` 里指向本类，
 * 否则 JUnit 用的还是默认 `AndroidJUnitRunner`，Hilt 不会生效。
 */
class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application =
        super.newApplication(cl, HiltTestApplication::class.java.name, context)
}

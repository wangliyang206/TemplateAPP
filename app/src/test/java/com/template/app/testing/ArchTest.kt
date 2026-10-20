package com.template.app.testing

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameContaining
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test
import java.io.File

/**
 * 架构守护测试（Konsist）。
 *
 * 把团队约定从"写在文档里靠人记"变成 CI 门禁：
 * - ViewModel 必须经 Repository 取数，禁止 import ApiService；
 * - UI 层（Activity / Fragment / Adapter）禁止接触网络栈（api / retrofit / okhttp）；
 * - 状态类命名统一（xxxState 结尾），保证 UiState 范式可检索；
 * - Repository 类只允许出现在 data / ui 包里。
 *
 * 规则与 docs 里的分层说明一一对应，改架构时同步更新这里。
 */
class ArchTest {

    private val scope = Konsist.scopeFromProject(File("..").absolutePath)

    private companion object {
        /**
         * 网络接口所在包。用通配片段而不是写死完整包名：
         * 开新项目改名后（com.template.app -> com.xxx.yyy）规则仍然生效，
         * 这是 .pro 文件踩过的坑，这里不再犯。
         */
        const val API_PACKAGE_SEGMENT = ".data.api."
    }

    /** ViewModel 不许直连 ApiService —— 数据必须走 Repository（UDF 数据单向流动） */
    @Test
    fun `ViewModel 不允许直接依赖 ApiService`() {
        scope
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue { clazz ->
                clazz.containingFile.imports.none { it.name.contains(API_PACKAGE_SEGMENT) }
            }
    }

    /** UI 层不许接触网络栈 —— 网络细节只能在 data / core 层出现 */
    @Test
    fun `UI 层不允许依赖网络栈`() {
        scope
            .classes()
            .withNameEndingWith("Activity")
            .plus(scope.classes().withNameEndingWith("Fragment"))
            .plus(scope.classes().withNameEndingWith("Adapter"))
            .assertTrue { clazz ->
                val imports = clazz.containingFile.imports.map { it.name }
                imports.none { name ->
                    name.contains(API_PACKAGE_SEGMENT) ||
                        name.startsWith("okhttp3.") ||
                        name.startsWith("retrofit2.")
                }
            }
    }

    /** 状态类命名统一：以 State 结尾，避免状态类命名漂移 */
    @Test
    fun `UiState 相关类命名统一以 State 结尾`() {
        scope
            .classes()
            .withPackage("..ui..")
            .withNameContaining("State")
            .assertTrue { it.name.endsWith("State") }
    }

    /** Repository 只在 data / ui 包里，禁止在其它位置冒出同类名 */
    @Test
    fun `Repository 类命名与位置统一`() {
        scope
            .classes()
            .withNameEndingWith("Repository")
            .assertTrue {
                it.resideInPackage("..ui..") || it.resideInPackage("..data..")
            }
    }
}

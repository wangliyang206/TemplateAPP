// Top-level build file
// 所有版本号统一维护在 gradle/libs.versions.toml，禁止在模块内硬编码版本。
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.daggerHiltAndroid) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.spotless) apply false
}

// ---------------------------------------------------------------------------
// 统一的代码规范 / 静态分析 / 覆盖率配置。
//
// 放在根项目统一 apply，而不是每个模块各写一份：
// 新加模块时不用记着"还要配 detekt"，否则规则会随时间流失。
// ---------------------------------------------------------------------------

subprojects {
    val moduleName = name

    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "org.jetbrains.kotlinx.kover")

    // detekt：基于默认规则集，只覆盖少数会误伤模板写法的项
    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension>("detekt") {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        parallel = true
        // 只分析主源码与测试源码，不扫 build 产物
        source.setFrom(
            files(
                "src/main/java",
                "src/main/kotlin",
                "src/test/java",
                "src/androidTest/java"
            )
        )
    }

    // spotless + ktlint：只管格式（缩进/换行/import 顺序），规则冲突交给 detekt
    extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension>("spotless") {
        kotlin {
            target("src/**/*.kt")
            ktlint(libs.versions.ktlint.get()).editorConfigOverride(
                mapOf(
                    // 模板里中文注释较多，关闭"注释行长度"限制避免噪音
                    "max_line_length" to "120",
                    "ktlint_standard_function-naming" to "disabled",
                    // 命名规则交给 detekt（naming 规则集），避免两套工具对
                    // KEY_TOKEN / INSTANCE 这类 Android 常见写法互相打架
                    "ktlint_standard_property-naming" to "disabled",
                    "ktlint_standard_no-wildcard-imports" to "disabled"
                )
            )
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(libs.versions.ktlint.get())
        }
    }

    // kover：生成每模块的覆盖率报告，并把阈值变成 CI 卡口（非回退底线）。
    //
    // 阈值口径：卡在「实测值留一点余量」，作用是防止覆盖率悄悄回退，而不是假装达标。
    // 当前实测（2026-10-22，补齐 SignUtils / 请求头链路 / SessionManager / Room DAO 之后）：
    //   core ≈ 39%（主要未覆盖：UI 组件、升级模块）
    //   app  ≈ 5%（UI 层为主，要提上去得靠 UI/截图测试）
    // 目标值：core ≥ 40%、app ≥ 15%。想提高门禁请先补测试，不要下调这里的数字。
    extensions.configure<kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension>("kover") {
        reports {
            total {
                html {
                    onCheck = false
                }
                xml {
                    onCheck = false
                }
            }
            verify {
                rule("$moduleName 行覆盖率非回退底线") {
                    minBound(if (moduleName == "core") 35 else 5)
                }
            }
        }
    }
}

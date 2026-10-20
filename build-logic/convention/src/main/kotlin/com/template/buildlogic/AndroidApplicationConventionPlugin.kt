package com.template.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * 所有 Android Application 模块的公共配置。
 * 模块脚本只保留自己特有内容（flavor / 签名 / buildConfigField），
 * 避免"第二个模块出现时配置就开始漂移"。
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.android")

            extensions.configure<ApplicationExtension> {
                compileSdk = ProjectConfig.COMPILE_SDK

                defaultConfig {
                    minSdk = ProjectConfig.MIN_SDK
                    targetSdk = ProjectConfig.TARGET_SDK
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                        excludes += "/META-INF/LICENSE*"
                    }
                }

                lint {
                    abortOnError = true
                    checkReleaseBuilds = true
                    // 告警即失败：把"0 warning"从口头约定变成 CI 卡口，
                    // 避免新告警随业务代码悄悄混入（与 README 的门禁描述一致）。
                    warningsAsErrors = true
                    // 版本升级由 Renovate 负责，不在 lint 里噪音告警
                    disable += setOf("GradleDependency", "AndroidGradlePluginVersion", "NewerVersionAvailable")
                }

                testOptions {
                    unitTests.isReturnDefaultValues = true
                }
            }

            configureKotlin()
        }
    }
}

/** Kotlin Android 模块统一 jvmTarget */
internal fun Project.configureKotlin() {
    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

package com.template.buildlogic

/**
 * 全模板统一的 SDK / JVM 版本真相源。
 * 模块脚本不再各写一份，升级时只改这里。
 */
object ProjectConfig {
    const val COMPILE_SDK = 35
    const val MIN_SDK = 24
    const val TARGET_SDK = 35
    const val JVM_TARGET = 17
}

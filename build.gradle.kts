// Top-level build file
// 所有版本号统一维护在 gradle/libs.versions.toml，禁止在模块内硬编码版本。
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.daggerHiltAndroid) apply false
}

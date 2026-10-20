import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.daggerHiltAndroid)
}

android {
    namespace = "com.template.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            consumerProguardFiles("consumer-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// Room 导出 schema，供迁移 diff 与 androidTest 使用
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// ---------------------------------------------------------------
// 依赖约定：
//   api            = 会出现在业务层公开 API 上的库（Retrofit / Room / Hilt 注解等），
//                    app 模块因此无需重复声明，避免版本漂移。
//   implementation = 纯内部实现细节。
// ---------------------------------------------------------------
dependencies {
    // AndroidX 基础
    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.androidx.activity.ktx)
    api(libs.androidx.fragment.ktx)
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.livedata.ktx)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.constraintlayout)
    api(libs.androidx.recyclerview)
    api(libs.androidx.swiperefreshlayout)
    api(libs.androidx.viewpager2)
    api(libs.androidx.material)
    api(libs.androidx.splashscreen)
    api(libs.androidx.datastore.preferences)

    // Room
    api(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt
    api(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Network
    api(libs.retrofit.core)
    api(libs.retrofit.gson)
    api(libs.okhttp.core)
    api(libs.gson)
    implementation(libs.okhttp.logging)

    // Coroutines
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)

    // UI / 工具
    api(libs.glide)
    api(libs.timber)
    api(libs.mmkv)
    api(libs.autosize)
    api(libs.immersionbar)
    api(libs.immersionbar.components)
    api(libs.smartrefresh.core)
    api(libs.smartrefresh.header)
    api(libs.smartrefresh.footer)
    api(libs.xxpermissions)
    api(libs.utilcodex)

    // 单元测试：core 的每个能力都应能在 JVM 上被验证
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.androidx.test.core)
}

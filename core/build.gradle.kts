plugins {
    // 公共 Library 配置（compileSdk / minSdk / compileOptions / jvmTarget）由 build-logic 统一提供
    alias(libs.plugins.templateAndroidLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.daggerHiltAndroid)
}

android {
    namespace = "com.template.core"

    defaultConfig {
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

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    testOptions {
        // Robolectric 需要真实资源来 inflate 布局（StatefulLayout / StateCardView 的回归测试依赖它）
        unitTests.isIncludeAndroidResources = true
    }

    // 把导出的 schema 挂到 test 的 assets 上，MigrationTestHelper 才能在 JVM 上
    // 按 `com.template.core.db.AppDatabase/<version>.json` 的路径读到它。
    // 不加这行就只能把迁移测试放到 androidTest（需要真机/模拟器），CI 跑不起来。
    sourceSets.getByName("test").assets.srcDir("$projectDir/schemas")
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
    api(libs.immersionbar)
    api(libs.immersionbar.components)
    api(libs.smartrefresh.core)
    api(libs.smartrefresh.header)
    api(libs.smartrefresh.footer)
    api(libs.xxpermissions)

    // 单元测试：core 的每个能力都应能在 JVM 上被验证
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.junit)
    // MigrationTestHelper：在 JVM（Robolectric）上验证 Room 迁移，不依赖真机
    testImplementation(libs.androidx.room.testing)
}

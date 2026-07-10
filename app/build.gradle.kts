plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kotlinKapt)
    alias(libs.plugins.daggerHiltAndroid)
}

apply(from = rootProject.file("config/buildConfig.gradle.kts"))

@Suppress("UNCHECKED_CAST")
val buildConfig: Map<String, String> = rootProject.extra["buildConfig"] as Map<String, String>

android {
    namespace = "com.template.app"
    compileSdk = 34

    defaultConfig {
        applicationId = buildConfig["applicationId"]
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = project.properties["SIGNING_KEY_ALIAS"] as? String ?: ""
            keyPassword = project.properties["SIGNING_KEY_PASSWORD"] as? String ?: ""
            storeFile = file(project.properties["SIGNING_STORE_FILE"] as? String ?: "keystore/release.jks")
            storePassword = project.properties["SIGNING_STORE_PASSWORD"] as? String ?: ""
        }
        getByName("debug") {
            storeFile = file(project.properties["DEBUG_STORE_FILE"] as? String ?: "keystore/debug.jks")
            keyAlias = project.properties["DEBUG_KEY_ALIAS"] as? String ?: "androiddebugkey"
            keyPassword = project.properties["DEBUG_KEY_PASSWORD"] as? String ?: "android"
            storePassword = project.properties["DEBUG_STORE_PASSWORD"] as? String ?: "android"
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = buildConfig["versionNameSuffix"]
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("String", "API_HOST", "\"${buildConfig["apiHost"]}\"")
            buildConfigField("String", "H5_HOST", "\"${buildConfig["h5Host"]}\"")
            buildConfigField("String", "APP_NAME", "\"${buildConfig["appName"]}\"")
            // 是否使用本地测试数据（开启后将跳过真实 API 调用，直接返回模拟数据）
            buildConfigField("boolean", "IS_DEBUG_DATA", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "API_HOST", "\"${buildConfig["apiHost"]}\"")
            buildConfigField("String", "H5_HOST", "\"${buildConfig["h5Host"]}\"")
            buildConfigField("String", "APP_NAME", "\"${buildConfig["appName"]}\"")
            // Release 版本禁用测试数据
            buildConfigField("boolean", "IS_DEBUG_DATA", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.material)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.paging.runtime)

    implementation(libs.glide)
    kapt(libs.glide.compiler)
    implementation(libs.timber)
    implementation(libs.mmkv)
    implementation(libs.autosize)
    implementation(libs.immersionbar)
    implementation(libs.smartrefresh.core)
    implementation(libs.smartrefresh.header)
    implementation(libs.smartrefresh.footer)
    implementation(libs.banner)
    implementation(libs.xxpermissions)
    implementation(libs.utilcodex)

    debugImplementation(libs.leakcanary)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso)
}

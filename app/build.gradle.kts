import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.daggerHiltAndroid)
}

// ---------------------------------------------------------------------------
// 签名配置：密钥文件与口令永不入库。
// 本地：写在 local.properties（已被 .gitignore 忽略）
// CI：  通过环境变量注入
// 缺失时不阻塞 debug 构建，但 release 包将是未签名产物。
// ---------------------------------------------------------------------------
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun secret(name: String): String? =
    (localProperties.getProperty(name) ?: System.getenv(name))?.takeIf { it.isNotBlank() }

val releaseStoreFile = secret("RELEASE_STORE_FILE")
val releaseStorePassword = secret("RELEASE_STORE_PASSWORD")
val releaseKeyAlias = secret("RELEASE_KEY_ALIAS")
val releaseKeyPassword = secret("RELEASE_KEY_PASSWORD")

val hasReleaseSigning = listOfNotNull(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword
).size == 4 && rootProject.file(releaseStoreFile!!).exists()

// ---------------------------------------------------------------------------
// 模板占位密钥闸门
// 模板自带 app/keystore/release.jks，但它在 git 历史里公开过，只能当占位用。
// 新项目如果直接复制本模板、又忘了重新生成密钥，会带着一把"人人皆知"的私钥上架。
// 所以这里做两件事：
//   1) 读实际密钥库的 SHA-256，判断是不是占位密钥；
//   2) 开关 template.usePlaceholderReleaseKey=false 时，发现仍是占位密钥就 fail。
// ---------------------------------------------------------------------------
val placeholderReleaseSha256 =
    "D5:BC:46:0C:72:2C:98:01:9E:15:16:77:A6:EA:BC:FB:E8:8D:F3:92:BA:C4:22:2F:C9:56:01:D5:80:1C:85:91"

val allowPlaceholderReleaseKey =
    (findProperty("template.usePlaceholderReleaseKey") as? String)?.toBoolean() ?: true

fun keystoreSha256(path: String, storePass: String, alias: String): String? = runCatching {
    // KeyStore.getInstance(File, char[]) 会自动探测 JKS / PKCS12 并完成加载（JDK 9+）
    val ks = KeyStore.getInstance(File(path), storePass.toCharArray())
    val cert = ks.getCertificate(alias) ?: return@runCatching null
    MessageDigest.getInstance("SHA-256")
        .digest(cert.encoded)
        .joinToString(":") { "%02X".format(it) }
}.getOrNull()

val releaseCertSha256: String? =
    if (hasReleaseSigning) {
        keystoreSha256(
            rootProject.file(releaseStoreFile!!).absolutePath,
            releaseStorePassword!!,
            releaseKeyAlias!!
        )
    } else {
        null
    }

val usingPlaceholderReleaseKey = releaseCertSha256 != null && releaseCertSha256 == placeholderReleaseSha256


// ---------------------------------------------------------------------------
// debug 签名：必须固定，否则每台机器用自己生成的 ~/.android/debug.keystore。
// 后果：同事之间互相覆盖安装会报 INSTALL_FAILED_UPDATE_INCOMPATIBLE，
//       并且按「包名 + 签名」校验的第三方 SDK（微信/地图/推送/分享）会失效。
// 团队 debug 密钥是标准 Android Debug 证书，不含业务机密，允许随仓库分发；
// release 密钥仍然只走 local.properties / CI Secrets。
// ---------------------------------------------------------------------------
val debugStoreFile = secret("DEBUG_STORE_FILE") ?: "app/keystore/debug.jks"
val debugStorePassword = secret("DEBUG_STORE_PASSWORD") ?: "android"
val debugKeyAlias = secret("DEBUG_KEY_ALIAS") ?: "androiddebugkey"
val debugKeyPassword = secret("DEBUG_KEY_PASSWORD") ?: "android"
val hasDebugSigning = rootProject.file(debugStoreFile).exists()

android {
    namespace = "com.template.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.template.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 请求签名密钥：不要写死在代码里，本地用 local.properties，CI 用环境变量
        buildConfigField("String", "SIGN_SECRET", "\"${secret("SIGN_SECRET").orEmpty()}\"")
    }

    signingConfigs {
        // debug 变体统一使用团队密钥（缺失时自动回退到 AGP 默认的 ~/.android/debug.keystore）
        getByName("debug").apply {
            if (hasDebugSigning) {
                storeFile = rootProject.file(debugStoreFile)
                storePassword = debugStorePassword
                keyAlias = debugKeyAlias
                keyPassword = debugKeyPassword
            } else {
                logger.warn(
                    "[signing] 未找到团队 debug 密钥 ${debugStoreFile}，将回退到本机默认 debug.keystore，" +
                        "多人协作时会导致覆盖安装失败。请从内部仓库获取 app/keystore/debug.jks。"
                )
            }
        }

        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    // 环境通过 flavor 表达，禁止再出现 "改一行 val env = 0 切环境" 的做法。
    // dev 与 prod 可同时安装（applicationIdSuffix 不同）。
    flavorDimensions += "env"
    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "TemplateAPP-Dev")
            buildConfigField("String", "API_HOST", "\"http://www.dagongji.xin/\"")
            buildConfigField("String", "H5_HOST", "\"https://h5-dev.template.com/\"")
            buildConfigField("boolean", "IS_DEBUG_DATA", "true")
        }
        create("staging") {
            dimension = "env"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            resValue("string", "app_name", "TemplateAPP-Staging")
            buildConfigField("String", "API_HOST", "\"https://api-staging.template.com/\"")
            buildConfigField("String", "H5_HOST", "\"https://h5-staging.template.com/\"")
            buildConfigField("boolean", "IS_DEBUG_DATA", "true")
        }
        create("prod") {
            dimension = "env"
            resValue("string", "app_name", "TemplateAPP")
            buildConfigField("String", "API_HOST", "\"https://api.template.com/\"")
            buildConfigField("String", "H5_HOST", "\"https://h5.template.com/\"")
            buildConfigField("boolean", "IS_DEBUG_DATA", "false")
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            // 显式绑定，避免不同 AGP 版本行为差异导致"以为用了团队密钥其实没有"
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if (hasReleaseSigning) signingConfigs.getByName("release") else null
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE*"
        }
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        warningsAsErrors = false
        // 版本升级由 Renovate / dependabot 负责，不在 lint 里噪音告警
        disable += setOf("GradleDependency", "AndroidGradlePluginVersion", "NewerVersionAvailable")
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

// CI 在打包 release 前执行，签名缺失时直接失败，避免"空口令签名"的产物流出。
tasks.register("verifyReleaseSigning") {
    group = "verification"
    description = "校验 release 签名配置是否完整、是否仍在用模板占位密钥"
    doLast {
        if (!hasReleaseSigning) {
            throw GradleException(
                """
                |缺少 release 签名配置，已中止。
                |请在 local.properties 或环境变量中提供：
                |  RELEASE_STORE_FILE / RELEASE_STORE_PASSWORD / RELEASE_KEY_ALIAS / RELEASE_KEY_PASSWORD
                |说明见 docs/SIGNING.md。
                """.trimMargin()
            )
        }

        if (usingPlaceholderReleaseKey && !allowPlaceholderReleaseKey) {
            throw GradleException(
                """
                |当前 release 密钥仍是模板占位密钥，但 template.usePlaceholderReleaseKey=false，已中止。
                |占位密钥（CN=beanSproutAssistant）曾出现在公开 git 历史中，任何拿到本仓库的人都能拿它签名，
                |绝不可用于对外发布。
                |
                |请重新生成密钥：
                |  keytool -genkeypair -v -keystore app/keystore/release.jks -alias youralias \
                |          -keyalg RSA -keysize 2048 -validity 10000
                |然后同步更新 local.properties 里的 RELEASE_* 四项。说明见 docs/SIGNING.md。
                """.trimMargin()
            )
        }

        if (usingPlaceholderReleaseKey) {
            logger.warn(
                """
                |
                |======================================================================
                |  ⚠  release 包正在使用【模板占位密钥】（CN=beanSproutAssistant）
                |     SHA-256: $releaseCertSha256
                |     密钥曾公开于 git 历史，仅可用于内部演示 / 模板自测。
                |     正式对外发布前必须重新生成 release.jks，
                |     并把 gradle.properties 的 template.usePlaceholderReleaseKey 改为 false。
                |======================================================================
                """.trimMargin()
            )
        } else {
            logger.lifecycle("[signing] release 使用自有密钥，SHA-256: $releaseCertSha256")
        }
    }
}

dependencies {
    implementation(project(":core"))

    // app 只为"业务专属"依赖负责；通用库由 core 以 api 暴露，避免重复声明
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.banner)

    // Hilt 的 Gradle 插件要求宿主模块显式声明 hilt-android（不能只靠 core 的 api 传递）
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    debugImplementation(libs.leakcanary)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.paging.testing)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso)
}

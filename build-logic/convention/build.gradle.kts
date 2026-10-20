plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

gradlePlugin {
    plugins {
        create("androidApplication") {
            id = "template.android.application"
            implementationClass = "com.template.buildlogic.AndroidApplicationConventionPlugin"
        }
        create("androidLibrary") {
            id = "template.android.library"
            implementationClass = "com.template.buildlogic.AndroidLibraryConventionPlugin"
        }
    }
}

// build-logic 编译期只需要 AGP 与 Kotlin 插件的 API。
// 版本与 gradle/libs.versions.toml 的 agp / kotlin 一致 —— 升级主工程版本时同步改这里。
dependencies {
    compileOnly("com.android.tools.build:gradle:8.7.3")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
}

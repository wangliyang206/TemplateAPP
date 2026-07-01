// Multi-environment configuration for TemplateAPP
// Usage in app/build.gradle.kts:
//   apply(from = rootProject.file("config/buildConfig.gradle.kts"))
// Then access: rootProject.extra["buildConfig"]
extra["environment"] = 0

val env = extra["environment"] as Int
val config = when (env) {
    0 -> mapOf(
        "appName" to "TemplateAPP-Dev",
        "applicationId" to "com.template.app.dev",
        "apiHost" to "https://api-dev.template.com/",
        "h5Host" to "https://h5-dev.template.com/",
        "versionNameSuffix" to "-dev"
    )
    1 -> mapOf(
        "appName" to "TemplateAPP-Test",
        "applicationId" to "com.template.app.test",
        "apiHost" to "https://api-test.template.com/",
        "h5Host" to "https://h5-test.template.com/",
        "versionNameSuffix" to "-test"
    )
    else -> mapOf(
        "appName" to "TemplateAPP",
        "applicationId" to "com.template.app",
        "apiHost" to "https://api.template.com/",
        "h5Host" to "https://h5.template.com/",
        "versionNameSuffix" to ""
    )
}
extra["buildConfig"] = config
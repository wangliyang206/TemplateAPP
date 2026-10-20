# core 模块的 consumer rules：随 AAR 传递给使用方，无需 app 重复配置。

# 网络层模型通过 Gson 反射序列化，字段名需保留
-keepclassmembers,allowobfuscation class com.template.core.network.** {
    <fields>;
}

# Timber 在 release 下用 DebugTree 以外的实现，无需特殊规则
# Hilt / Room / Retrofit / OkHttp 均自带 consumer rules

# core 模块的 consumer rules：随 AAR 传递给使用方，无需 app 重复配置。

# 网络层模型通过 Gson 反射序列化，字段名需保留。
# 同样**不写完整包名**，用 `**.network.**` 匹配任意前缀，保证模板改名后规则依然生效。
-keepclassmembers,allowobfuscation class **.network.** {
    <fields>;
}

# Timber 在 release 下用 DebugTree 以外的实现，无需特殊规则
# Hilt / Room / Retrofit / OkHttp 均自带 consumer rules

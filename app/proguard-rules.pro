# ---------------------------------------------------------------------------
# R8 规则原则：能用反射访问的东西才 keep，禁止整包 keep。
# 整包 keep 会让 isMinifyEnabled 形同虚设（体积不降、反编译照样能读）。
# ---------------------------------------------------------------------------

# 保留泛型签名与注解，Retrofit / Gson 依赖它们做类型解析
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault

# ---------- Retrofit ----------
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keepclasseswithmembers,includedescriptorclasses class * {
    @retrofit2.http.* <methods>;
}

# ---------- Gson ----------
# 序列化依赖字段名，字段名不能被混淆；类名可以。
# 关键：这里**不写完整包名**，只用 `**.data.model.**` 这种"任意前缀"匹配。
# 原因：exe 模板改名（com.template.app → com.yourco.xxx）后，写死包名的规则会静默失效，
# 导致 release 包 Gson 字段被混淆、接口全部解析失败，而 debug 不混淆所以本地测不出来。
-keepclassmembers,allowobfuscation class **.data.model.** {
    <fields>;
}
# 网络层的请求/响应壳（ApiRequest / BaseResponse）同样走 Gson
-keepclassmembers,allowobfuscation class **.data.api.** {
    <fields>;
}
-keepclassmembers,allowobfuscation class **.network.** {
    <fields>;
}
# 枚举按名字序列化
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ---------- 崩溃栈可读性 ----------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

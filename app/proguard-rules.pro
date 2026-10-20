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
# 序列化依赖字段名，字段名不能被混淆；类名可以
-keepclassmembers,allowobfuscation class com.template.app.data.model.** {
    <fields>;
}
-keepclassmembers,allowobfuscation class com.template.core.network.** {
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

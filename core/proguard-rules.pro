# core module 混淆规则
#
# 这里**故意留空**，原因：
# 1. 库模块真正生效的规则是 consumer-rules.pro（随 AAR 传给使用方）；
#    本文件只在 core 自己开 minify 时才有意义，而 core 的 isMinifyEnabled 一直是 false。
# 2. 历史上这里有一句 `-keep public class com.template.core.** { *; }`：
#    - 它把整个 core 包全部保留，R8 对 core 的优化形同关闭；
#    - 它写死了包名，模板改名后会静默失效（更糟：没人会发现规则没生效）。
#
# 需要传给使用方的规则请写到 consumer-rules.pro。

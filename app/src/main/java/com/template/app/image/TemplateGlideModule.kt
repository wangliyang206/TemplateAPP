package com.template.app.image

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule

/**
 * 全局 Glide 配置。
 *
 * 为什么需要它：
 * 1. 没有 `@GlideModule` 时，Glide 会在运行时打
 *    `Failed to find GeneratedAppGlideModule` 告警，且所有 `LibraryGlideModule` 被静默忽略；
 * 2. 缓存大小/磁盘策略默认值偏保守，模板里固定成可预期的数值，方便按项目调。
 *
 * 注意：本类必须放在**带 applicationId 的模块**（app），且该模块要挂
 * `ksp(libs.glide.ksp)`，否则不会生成 `GeneratedAppGlideModuleImpl`。
 */
@GlideModule
class TemplateGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // 内存缓存取可用内存的 20%（Glide 默认 20%，这里显式写出便于按机型调整）
        val memoryCacheSize = (Runtime.getRuntime().maxMemory() / MEMORY_CACHE_DIVISOR).toInt()
        builder.setMemoryCache(LruResourceCache(memoryCacheSize.toLong()))

        // 磁盘缓存 100MB，放在应用私有目录，卸载即清理
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, DISK_CACHE_SIZE))
    }

    // 库模块若自带 LibraryGlideModule，需要在这里返回 true 让 Glide 扫描；
    // 本模板没有，显式返回 false 可减少一次类扫描（启动更快）。
    override fun isManifestParsingEnabled(): Boolean = false

    private companion object {
        const val DISK_CACHE_SIZE = 100L * 1024 * 1024
        const val MEMORY_CACHE_DIVISOR = 5
    }
}

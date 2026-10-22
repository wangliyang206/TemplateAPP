package com.template.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 业务设置的内存模型。
 *
 * **默认值写在这里**（而不是散落在读取处），保证"没存过"和"存过但为 true"表现一致。
 */
data class AppSettings(val wifiOnlyDownload: Boolean = true, val pushEnabled: Boolean = true)

/**
 * 业务设置存储 —— DataStore 的业务键用法参考实现。
 *
 * 和 `DataStoreManager`（存 token / 用户资料，属于登录态）分开是**刻意**的：
 * - 两者生命周期不同：登录态会随登出清空，设置不该被清；
 * - 两者演化节奏不同：设置项频繁增删，不该每次都给会话存储加迁移负担。
 *
 * 新增一个设置项 = 加一个 key + 在 [settings] 里映射 + 加一个 setter，不需要改数据库。
 */
@Singleton
class SettingsStore @Inject constructor(@ApplicationContext private val context: Context) {

    private val dataStore: DataStore<Preferences> = context.settingsDataStore

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            wifiOnlyDownload = prefs[KEY_WIFI_ONLY_DOWNLOAD] ?: true,
            pushEnabled = prefs[KEY_PUSH_ENABLED] ?: true,
        )
    }

    suspend fun setWifiOnlyDownload(enabled: Boolean) {
        dataStore.edit { it[KEY_WIFI_ONLY_DOWNLOAD] = enabled }
    }

    suspend fun setPushEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_PUSH_ENABLED] = enabled }
    }

    private companion object Keys {
        val KEY_WIFI_ONLY_DOWNLOAD = booleanPreferencesKey("wifi_only_download")
        val KEY_PUSH_ENABLED = booleanPreferencesKey("push_enabled")
    }
}

/** 业务设置单独的 DataStore 文件，与会话存储互不影响 */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

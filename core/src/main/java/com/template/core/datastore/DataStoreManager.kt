package com.template.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "template_app_prefs")

class DataStoreManager(private val context: Context) {

    private val dataStore = context.dataStore

    companion object Keys {
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_TOKEN = stringPreferencesKey("token")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_PHONE = stringPreferencesKey("user_phone")
        val KEY_USER_AVATAR = stringPreferencesKey("user_avatar")
    }

    suspend fun saveUser(userId: String, token: String, userName: String, phone: String = "", avatar: String? = null) {
        dataStore.edit {
            it[KEY_USER_ID] = userId
            it[KEY_TOKEN] = token
            it[KEY_USER_NAME] = userName
            it[KEY_USER_PHONE] = phone
            if (avatar != null) {
                it[KEY_USER_AVATAR] = avatar
            } else {
                it.remove(KEY_USER_AVATAR)
            }
        }
    }

    suspend fun clearUser() {
        dataStore.edit {
            it.remove(KEY_USER_ID)
            it.remove(KEY_TOKEN)
            it.remove(KEY_USER_NAME)
            it.remove(KEY_USER_PHONE)
            it.remove(KEY_USER_AVATAR)
        }
    }

    val userFlow: Flow<UserPref> = dataStore.data.map {
        UserPref(
            userId = it[KEY_USER_ID] ?: "",
            token = it[KEY_TOKEN] ?: "",
            userName = it[KEY_USER_NAME] ?: "",
            phone = it[KEY_USER_PHONE] ?: "",
            avatar = it[KEY_USER_AVATAR] ?: ""
        )
    }

    /** 同步读取（仅用于 OkHttp Interceptor 等无法挂起的地方） */
    suspend fun getUserSync(): UserPref {
        val preferences = dataStore.data.first()
        return UserPref(
            userId = preferences[KEY_USER_ID] ?: "",
            token = preferences[KEY_TOKEN] ?: "",
            userName = preferences[KEY_USER_NAME] ?: "",
            phone = preferences[KEY_USER_PHONE] ?: "",
            avatar = preferences[KEY_USER_AVATAR] ?: ""
        )
    }

    data class UserPref(
        val userId: String,
        val token: String,
        val userName: String,
        val phone: String,
        val avatar: String
    )
}
package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.User
import com.example.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_ROLE = stringPreferencesKey("user_role")
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_BASE_URL = stringPreferencesKey("base_url")
        private val KEY_ONLINE_MODE = booleanPreferencesKey("online_mode")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_LOGGED_IN] ?: false
    }

    val currentUser: Flow<User?> = context.dataStore.data.map { preferences ->
        val id = preferences[KEY_USER_ID] ?: return@map null
        val name = preferences[KEY_USER_NAME] ?: "User"
        val email = preferences[KEY_USER_EMAIL] ?: ""
        val roleStr = preferences[KEY_USER_ROLE] ?: UserRole.ADMIN.name
        val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.ADMIN }
        val token = preferences[KEY_AUTH_TOKEN]
        User(id = id, name = name, email = email, role = role, token = token)
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DARK_MODE] ?: false
    }

    val isOnlineMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONLINE_MODE] ?: true
    }

    val baseUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_BASE_URL] ?: "https://domain-saya.com/api/"
    }

    suspend fun saveUserSession(user: User, token: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_LOGGED_IN] = true
            preferences[KEY_USER_ID] = user.id
            preferences[KEY_USER_NAME] = user.name
            preferences[KEY_USER_EMAIL] = user.email
            preferences[KEY_USER_ROLE] = user.role.name
            preferences[KEY_AUTH_TOKEN] = token
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_LOGGED_IN] = false
            preferences.remove(KEY_USER_ID)
            preferences.remove(KEY_USER_NAME)
            preferences.remove(KEY_USER_EMAIL)
            preferences.remove(KEY_USER_ROLE)
            preferences.remove(KEY_AUTH_TOKEN)
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DARK_MODE] = enabled
        }
    }

    suspend fun setOnlineMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONLINE_MODE] = enabled
        }
    }

    suspend fun setBaseUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BASE_URL] = url
        }
    }
}

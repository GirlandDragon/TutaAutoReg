package com.tuta.auto.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferenceManager(private val context: Context) {

    companion object {
        private val THEME_MODE_KEY = intPreferencesKey("theme_mode")
        private val CURRENT_ACCOUNT_ID_KEY = longPreferencesKey("current_account_id")
    }

    enum class ThemeMode(val value: Int) {
        SYSTEM(0),
        LIGHT(1),
        DARK(2);

        companion object {
            fun fromValue(value: Int): ThemeMode =
                entries.firstOrNull { it.value == value } ?: SYSTEM
        }
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        ThemeMode.fromValue(prefs[THEME_MODE_KEY] ?: 0)
    }

    val currentAccountId: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[CURRENT_ACCOUNT_ID_KEY]
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = mode.value
        }
    }

    suspend fun setCurrentAccountId(id: Long?) {
        context.dataStore.edit { prefs ->
            if (id != null) prefs[CURRENT_ACCOUNT_ID_KEY] = id
            else prefs.remove(CURRENT_ACCOUNT_ID_KEY)
        }
    }
}

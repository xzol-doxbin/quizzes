package eu.tutorials.mywishlistapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    companion object {
        private val KEY_USER_ID  = intPreferencesKey("user_id")
        private val KEY_USERNAME = stringPreferencesKey("username")
    }

    val userId: Flow<Int>
        get() = context.dataStore.data.map { it[KEY_USER_ID] ?: -1 }

    val username: Flow<String>
        get() = context.dataStore.data.map { it[KEY_USERNAME] ?: "" }

    suspend fun isLoggedIn(): Boolean {
        return context.dataStore.data
            .map { it[KEY_USER_ID] ?: -1 }
            .first() > 0
    }

    suspend fun saveSession(userId: Int, username: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID]  = userId
            prefs[KEY_USERNAME] = username
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
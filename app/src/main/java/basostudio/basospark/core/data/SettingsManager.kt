package basostudio.basospark.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import basostudio.basospark.ui.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Khởi tạo DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }

    // Luồng (Flow) để lắng nghe sự thay đổi của theme
    val themeModeFlow = dataStore.data.map { preferences ->
        ThemeMode.valueOf(preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name)
    }

    // Hàm để lưu lựa chọn theme
    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { settings ->
            settings[THEME_MODE_KEY] = themeMode.name
        }
    }
}
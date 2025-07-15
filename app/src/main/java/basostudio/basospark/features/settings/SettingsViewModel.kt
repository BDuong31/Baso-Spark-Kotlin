package basostudio.basospark.features.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basostudio.basospark.core.data.SessionManager
import basostudio.basospark.core.data.SettingsManager
import basostudio.basospark.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    application: Application,
    private val sessionManager: SessionManager
) : ViewModel() {
    val themeMode = settingsManager.themeModeFlow

    fun onLogout() {
        sessionManager.clearSession()
    }

    fun onThemeChange(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsManager.setThemeMode(themeMode)
        }
    }
}
package basostudio.basospark

import androidx.lifecycle.ViewModel
import basostudio.basospark.core.data.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsManager: SettingsManager
) : ViewModel() {
    val themeMode = settingsManager.themeModeFlow
}
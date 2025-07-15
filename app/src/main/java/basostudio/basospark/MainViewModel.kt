package basostudio.basospark

import androidx.lifecycle.ViewModel
import basostudio.basospark.core.data.SettingsManager
import basostudio.basospark.core.network.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val socketManager: SocketManager

) : ViewModel() {
    val themeMode = settingsManager.themeModeFlow

    init {
        socketManager.connect()
    }
}
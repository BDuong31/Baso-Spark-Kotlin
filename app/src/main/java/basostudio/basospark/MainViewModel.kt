package basostudio.basospark

import androidx.lifecycle.ViewModel
import basostudio.basospark.core.data.SessionManager
import basostudio.basospark.core.data.SettingsManager
import basostudio.basospark.core.network.SocketManager
import basostudio.basospark.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val socketManager: SocketManager,
    private val sessionManager: SessionManager
) : ViewModel() {
    val themeMode = settingsManager.themeModeFlow
    val currentUser: StateFlow<User?> = sessionManager.userDetailsFlow
    init {
        socketManager.connect()
    }

    fun logout() {
        socketManager.disconnect()
        sessionManager.clearSession()
    }
}

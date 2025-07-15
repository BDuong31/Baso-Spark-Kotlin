package basostudio.basospark.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateFcmTokenDto(
    val fcmToken: String
)
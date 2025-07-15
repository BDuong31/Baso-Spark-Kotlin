// Đặt file này trong package data/remote/dto hoặc data/model
package basostudio.basospark.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class này tương đương với userUpdateDTOSchema của bạn.
 * Nó dùng để gửi dữ liệu lên server khi người dùng cập nhật hồ sơ.
 *
 * @Serializable cho phép thư viện Kotlinx Serialization tự động chuyển đổi nó thành JSON.
 * Tất cả các thuộc tính đều là nullable và có giá trị mặc định là null,
 * tương đương với phương thức .partial() trong Zod.
 */
@Serializable
data class UserUpdateDto(

    @SerialName("firstName") // Ánh xạ tới tên trường trong JSON nếu cần
    val firstName: String? = null,

    @SerialName("lastName")
    val lastName: String? = null,

    @SerialName("username")
    val username: String? = null,

    @SerialName("bio")
    val bio: String? = null,

    @SerialName("websiteUrl")
    val link: String? = null,

    @SerialName("avatar")
    val avatar: String? = null, // URL mới của ảnh đại diện

    @SerialName("cover")
    val cover: String? = null // URL mới của ảnh bìa
)
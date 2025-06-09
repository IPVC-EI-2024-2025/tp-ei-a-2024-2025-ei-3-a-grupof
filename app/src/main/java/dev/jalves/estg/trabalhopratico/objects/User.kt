package dev.jalves.estg.trabalhopratico.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskSyncUser (
    val uid: String = "",
    var email: String = "",
    @SerialName("display_name")
    val displayName: String = "",
    val username: String = "",
    @SerialName("profile_picture")
    val profilePicture: String? = null,
    val role: String = "user"
) {
    companion object {
        fun fromView(data: Map<String, Any?>): TaskSyncUser {
            val metadata = data["raw_user_meta_data"] as? Map<*, *> ?: emptyMap<Any, Any>()

            return TaskSyncUser(
                uid = data["id"].toString().removeSurrounding("\""),
                email = data["email"].toString().removeSurrounding("\""),
                displayName = metadata["display_name"].toString().removeSurrounding("\""),
                username = metadata["username"].toString().removeSurrounding("\""),
                profilePicture = metadata["profile_picture"].toString().removeSurrounding("\""),
            )
        }
    }
}
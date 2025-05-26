package dev.jalves.estg.trabalhopratico.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskSyncUser (
    val uid: String = "",
    val email: String = "",
    @SerialName("display_name")
    val displayName: String = "",
    val username: String = "",
    @SerialName("profile_picture")
    val profilePicture: String = "",
    val role: String = "user"
)
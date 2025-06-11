package dev.jalves.estg.trabalhopratico.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskSyncUser (
    val id: String = "",
    var email: String = "",
    @SerialName("display_name")
    val displayName: String = "",
    val username: String = "",
    val role: String = "user"
)
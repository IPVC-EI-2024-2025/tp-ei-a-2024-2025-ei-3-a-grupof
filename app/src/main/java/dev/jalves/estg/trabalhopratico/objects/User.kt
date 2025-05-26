package dev.jalves.estg.trabalhopratico.objects

data class TaskSyncUser (
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val username: String = "",
    val profilePicture: String = "",
    val role: String = "user"
)
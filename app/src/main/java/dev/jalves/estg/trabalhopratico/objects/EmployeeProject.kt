package dev.jalves.estg.trabalhopratico.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmployeeProject(
    @SerialName("project_id")
    val projectId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("assigned_at")
    val assignedAt: String
)
package dev.jalves.estg.trabalhopratico.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateEmployeeProjectDTO(
    @SerialName("project_id")
    val projectId: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("assigned_at")
    val assignedAt: String = ""
)

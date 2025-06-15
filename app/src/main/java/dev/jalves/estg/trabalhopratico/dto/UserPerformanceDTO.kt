package dev.jalves.estg.trabalhopratico.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserPerformanceDTO(
    @SerialName("project_id")
    val projectId: String,
    @SerialName("user_id")
    val userId: String,
    val rating: Float,
    val comments: String?,
    @SerialName("evaluated_at")
    val evaluatedAt: String? = null,
    @SerialName("evaluated_by")
    val evaluatedBy: String
)

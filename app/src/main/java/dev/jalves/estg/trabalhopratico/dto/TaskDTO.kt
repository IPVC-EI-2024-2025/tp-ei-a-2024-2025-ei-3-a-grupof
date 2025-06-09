package dev.jalves.estg.trabalhopratico.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskDTO(
    @SerialName("project_id")
    val projectId: String,
    val name: String,
    val description: String,
    val status: String = "pending",
    @SerialName("created_by")
    val createdBy: String
)

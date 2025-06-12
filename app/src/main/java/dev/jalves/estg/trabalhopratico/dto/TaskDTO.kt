package dev.jalves.estg.trabalhopratico.dto

import dev.jalves.estg.trabalhopratico.objects.TaskStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskDTO(
    @SerialName("project_id")
    val projectId: String = "",
    val name: String,
    val description: String,
    val status: TaskStatus = TaskStatus.IN_PROGRESS,
    @SerialName("created_by")
    val createdBy: String = ""
)
data class UpdateTask(
    val description: String,
    val status: TaskStatus,
    val updatedAt: String = "",
    )
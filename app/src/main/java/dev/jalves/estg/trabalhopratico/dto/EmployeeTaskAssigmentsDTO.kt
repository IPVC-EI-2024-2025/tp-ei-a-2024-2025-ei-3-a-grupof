package dev.jalves.estg.trabalhopratico.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskAssignmentDTO(
    @SerialName("task_id")
    val taskId: String,
    @SerialName("employee_id")
    val employeeId: String,
    @SerialName("completion_rate")
    val completionRate: Float = 0.0f,
    @SerialName("completed_at")
    val completedAt: String? = null
)

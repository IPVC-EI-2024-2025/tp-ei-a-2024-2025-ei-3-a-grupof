package dev.jalves.estg.trabalhopratico.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmployeeTaskAssignment(
    @SerialName("task_id")
    val taskId: String,
    @SerialName("employee_id")
    val employeeId: String,
    @SerialName("completion_rate")
    val completionRate: Float,
    @SerialName("completed_at")
    val completedAt: String? = null
)
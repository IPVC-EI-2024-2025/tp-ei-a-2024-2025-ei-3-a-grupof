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

@Serializable
data class UpdateTask(
    val id: String,
    val name: String? = null,
    val description: String? = null,
    val status: String? = null
)

@Serializable
data class TaskOverviewDTO(
    val task: TaskInfoDTO,
    val employees: List<UserBasicDTO>?,
    @SerialName("task_logs")
    val taskLogs: List<TaskLogDTO>?
)

@Serializable
data class TaskInfoDTO(
    val id: String,
    val name: String?,
    val description: String?,
    val status: TaskStatus?,
    @SerialName("created_at")
    val createdAt: String?,
    @SerialName("updated_at")
    val updatedAt: String?,
    val project: ProjectBasicDTO,
    @SerialName("created_by")
    val createdBy: String?
)

@Serializable
data class ProjectBasicDTO(
    val id: String,
    val name: String
)

@Serializable
data class UserBasicDTO(
    val id: String,
    val username: String,
    @SerialName("display_name")
    val displayName: String,
    val email: String
)

@Serializable
data class TaskLogDTO(
    @SerialName("user_name")
    val userName: String,
    @SerialName("task_id")
    val taskId: String,
    val date: String?,
    val location: String?,
    @SerialName("completion_rate")
    val completionRate: Double,
    @SerialName("time_spent")
    val timeSpent: Double,
    val notes: String?,
    @SerialName("created_at")
    val createdAt: String?
)
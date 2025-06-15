package dev.jalves.estg.trabalhopratico.dto

import dev.jalves.estg.trabalhopratico.objects.Role
import dev.jalves.estg.trabalhopratico.objects.TaskStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateProjectDTO (
    val name: String,
    val description: String,
    @SerialName("start_date")
    val startDate: String,
    @SerialName("due_date")
    val dueDate: String,
    val managerID: String? = null
)

@Serializable
data class UpdateProjectDTO (
    val id: String,
    val name: String? = null,
    val description: String? = null,
    val startDate: String? = null,
    val dueDate: String? = null,
    val status: String? = null,
    val managerID: String? = null,
)

@Serializable
data class UserDTO (
    val id: String,
    @SerialName("display_name")
    val displayName: String,
    val username: String,
    val role: Role
)

@Serializable
data class ProjectOverviewDTO (
    val project: ProjectInfoDTO,
    val employees: List<ProjectEmployeeDTO>?,
    val tasks: List<ProjectTaskDTO>?
)

@Serializable
data class ProjectInfoDTO (
    val id: String,
    val name: String,
    val description: String,
    @SerialName("start_date")
    val startDate: String?,
    @SerialName("due_date")
    val dueDate: String?,
    val status: String,
    @SerialName("created_by_id")
    val createdById: String,
    val manager: UserBasicDTO?
)

@Serializable
data class ProjectEmployeeDTO (
    val id: String,
    val username: String,
    @SerialName("display_name")
    val displayName: String,
    val email: String,
    @SerialName("assigned_at")
    val assignedAt: String?
)

@Serializable
data class ProjectTaskDTO (
    val id: String,
    val name: String,
    val description: String,
    val status: String,
    @SerialName("created_at")
    val createdAt: String?,
    @SerialName("updated_at")
    val updatedAt: String?,
    @SerialName("created_by")
    val createdBy: String,
    @SerialName("assigned_employees")
    val assignedEmployees: List<UserBasicDTO>?,
    @SerialName("task_logs")
    val taskLogs: List<TaskLogDTO>?
)



@Serializable
data class ProjectDTO (
    val id: String = "",
    val name: String = "",
    val description: String = "",
    @SerialName("start_date")
    val startDate: String = "",
    @SerialName("due_date")
    val dueDate: String = "",
    val status: String = "",
    val manager: UserDTO?,
    val employees: List<UserDTO>
)
package dev.jalves.estg.trabalhopratico.dto

import dev.jalves.estg.trabalhopratico.objects.Role
import dev.jalves.estg.trabalhopratico.objects.User
import dev.jalves.estg.trabalhopratico.objects.TaskStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserDTO(
    @SerialName("display_name")
    val displayName: String,
    val email: String,
    val username: String,
    val password: String,
    val role: Role = Role.EMPLOYEE,
)

@Serializable
data class UpdateUserDTO(
    val id: String,
    @SerialName("display_name")
    val displayName: String? = null,
    val email: String? = null,
    val username: String? = null,
    val password: String? = null,
    @SerialName("profile_picture")
    val profilePicture: String? = null,
    val role: Role? = null,
    val status: Boolean? = true
)

@Serializable
data class UserOverviewDTO(
    val user: User,
    val tasks: List<UserTaskAssignmentDTO>,
    @SerialName("task_logs")
    val taskLogs: List<UserTaskLogDTO>,
    val projects: List<UserProjectDetailsDTO>,
    @SerialName("performance_reviews")
    val performanceReviews: List<UserPerformanceDTO>,

)

@Serializable
data class UserTaskAssignmentDTO(
    @SerialName("assignment_id")
    val assignmentId: String,
    @SerialName("task_id")
    val taskId: String,
    @SerialName("assigned_at")
    val assignedAt: String?,
    @SerialName("completion_rate")
    val completionRate: Double?,
    @SerialName("completed_at")
    val completedAt: String?,
)



@Serializable
data class UserTaskLogDTO(
    val Location: String?,
    val Notes: String?
)


@Serializable
data class UserProjectDetailsDTO(
    val name: String?,
    val description: String?,
    val status: String?,
)

@Serializable
data class UserPerformanceDTO(
    val rating: Double?,
    val comments: String?,
)







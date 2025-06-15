package dev.jalves.estg.trabalhopratico.objects

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import dev.jalves.estg.trabalhopratico.R

@Serializable
data class Task (
    val id: String = "",
    @SerialName("project_id")
    val projectId: String = "",
    val name: String,
    val description: String,
    val status: TaskStatus = TaskStatus.IN_PROGRESS,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("created_by")
    val createdBy: String = ""
)

@Serializable
enum class TaskStatus(
    val value: String,
    val descriptionId: Int,
    val color: Color
) {
    @SerialName("In Progress")
    IN_PROGRESS("In Progress", R.string.task_status_in_progress_desc, Color.hsl(122f, 0.39f, 0.59f, 0.1f)),

    @SerialName("Complete")
    COMPLETE("Complete", R.string.task_status_complete_desc, Color.hsl(40f, 0.75f, 0.75f));

    companion object {
        fun fromString(value: String): TaskStatus? {
            return entries.find { it.value.equals(value, ignoreCase = true) }
        }
    }
}
package dev.jalves.estg.trabalhopratico.dto

data class CreateTaskAssignmentDTO(
val taskId: String,
val employeeId:String,
val assigmentId:String,
    val assignedAt: String,
    val completionRate: Float ,
    val completedAt: String
)

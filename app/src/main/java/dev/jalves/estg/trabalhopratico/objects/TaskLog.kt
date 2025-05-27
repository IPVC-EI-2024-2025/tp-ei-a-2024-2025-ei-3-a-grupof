package dev.jalves.estg.trabalhopratico.objects

data class TaskLog (

    val userId: String,
    val taksId: String,
    val logId: String,
    val date: String,
    val location: String,
    val completionRate: Float,
    val timeSpent: Float,
    val roles: String

    )
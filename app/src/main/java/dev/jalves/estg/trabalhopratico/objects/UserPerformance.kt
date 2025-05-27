package dev.jalves.estg.trabalhopratico.objects

data class UserPerformance (

    val projectId: String,
    val userId: String,
    val performanceId: String,
    val rating: Float,
    val comments: String,
    val evaluatedAt: String,
    val evaluatedBy: String
)
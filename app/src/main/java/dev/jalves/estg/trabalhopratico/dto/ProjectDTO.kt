package dev.jalves.estg.trabalhopratico.dto

data class CreateProjectDTO (
    val name: String,
    val description: String,
    val startDate: String,
    val dueDate: String
)
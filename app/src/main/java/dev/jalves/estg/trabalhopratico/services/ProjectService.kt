package dev.jalves.estg.trabalhopratico.services

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import dev.jalves.estg.trabalhopratico.dto.CreateProjectDTO
import dev.jalves.estg.trabalhopratico.objects.Project
import kotlinx.coroutines.tasks.await

object ProjectService {
    @SuppressLint("StaticFieldLeak")
    private val db = Firebase.firestore

    suspend fun createProject(projectDto: CreateProjectDTO): Result<Unit> {
        val docRef = Firebase.firestore.collection("projects").document()

        val project = Project(
            id = docRef.id,
            name = projectDto.name,
            description = projectDto.description,
            startDate = projectDto.startDate,
            dueDate = projectDto.dueDate,
            status = "unknown",
            createdByID = Firebase.auth.currentUser?.uid ?: "",
            managerID = null
        )

        return try {
            db.collection("projects").document(project.id).set(project).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserService", "Failed to create profile", e)
            Result.failure(e)
        }
    }
}
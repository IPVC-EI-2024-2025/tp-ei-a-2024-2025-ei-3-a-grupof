package dev.jalves.estg.trabalhopratico.services

import android.content.ContentValues.TAG
import android.util.Log
import dev.jalves.estg.trabalhopratico.dto.CreateProjectDTO
import dev.jalves.estg.trabalhopratico.dto.CreateTaskDTO
import dev.jalves.estg.trabalhopratico.objects.Project
import dev.jalves.estg.trabalhopratico.objects.Task
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TaskService {

    suspend fun createTask(dto: CreateTaskDTO,id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val currentUserId = supabase.auth.currentUserOrNull()?.id
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val task = Task(
                    name = dto.name,
                    description = dto.description,
                    projectId = id,
                    status = dto.status,
                    createdBy = currentUserId,

                )

                supabase.from("projects").insert(task)

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create project", e)
                Result.failure(e)
            }
        }

}
package dev.jalves.estg.trabalhopratico.services

import android.content.ContentValues.TAG
import android.util.Log
import dev.jalves.estg.trabalhopratico.dto.CreateTaskDTO

import dev.jalves.estg.trabalhopratico.dto.UpdateTask
import dev.jalves.estg.trabalhopratico.objects.Task
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


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

                supabase.from("tasks").insert(task)

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create project", e)
                Result.failure(e)
            }
        }

    suspend fun listTasks(): Result<List<Task>> =
        withContext(Dispatchers.IO) {
            try {
                val projects = supabase.from("tasks")
                    .select {
                    }
                    .decodeList<Task>()

                Result.success(projects)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch tasks", e)
                Result.failure(e)
            }
        }



    suspend fun listProjectTasks(id:String): Result<List<Task>> =
        withContext(Dispatchers.IO) {
            try {
                val projects = supabase.from("tasks")
                    .select {
                    }
                    .decodeList<Task>()
                val goodUsers = mutableListOf<Task>()


                projects.forEach { project ->
                    val projectid = project.projectId
                    if (projectid == id) {
                        goodUsers.add(project
                        )
                    }
                }
                Result.success(goodUsers)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch tasks", e)
                Result.failure(e)
            }
        }

    suspend fun updateTask(updatedTask: UpdateTask,id: String ) =
        withContext(Dispatchers.IO) {
            try {
                val currentMoment = Clock.System.now()
                val dateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
                supabase.from("tasks").update(
                    buildMap {
                        put("updated_at", dateTime)
                        put("description", updatedTask.description)
                        put("status", updatedTask.status.value)
                    }
                ) {
                    filter {
                        eq("id", id)
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update project", e)
                Result.failure(e)
            }
        }


}
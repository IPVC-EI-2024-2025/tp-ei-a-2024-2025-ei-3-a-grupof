package dev.jalves.estg.trabalhopratico.services

import android.content.ContentValues.TAG
import android.util.Log
import dev.jalves.estg.trabalhopratico.dto.CreateProjectDTO
import dev.jalves.estg.trabalhopratico.objects.Project
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ProjectService {
    suspend fun createProject(projectDto: CreateProjectDTO): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val currentUserId = supabase.auth.currentUserOrNull()?.id
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val project = Project(
                    name = projectDto.name,
                    description = projectDto.description,
                    startDate = projectDto.startDate,
                    dueDate = projectDto.dueDate,
                    status = "unknown",
                    createdByID = currentUserId,
                    managerID = null
                )

                supabase.from("projects").insert(project)

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create project", e)
                Result.failure(e)
            }
        }

    suspend fun listProjects(): Result<List<Project>> =
        withContext(Dispatchers.IO) {
            try {
                val projects = supabase.from("projects")
                    .select {
                    }
                    .decodeList<Project>()

                Result.success(projects)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch projects", e)
                Result.failure(e)
            }
        }

    suspend fun listProjectsByCreator(creatorId: String): Result<List<Project>> =
        withContext(Dispatchers.IO) {
            try {
                val projects = supabase.from("projects")
                    .select {
                        filter {
                            eq("created_by_id", creatorId)
                        }
                    }
                    .decodeList<Project>()

                Result.success(projects)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch projects for creator $creatorId", e)
                Result.failure(e)
            }
        }

    suspend fun getProjectByID(projectID: String): Result<Project> =
        withContext(Dispatchers.IO) {
            try {
                val project = supabase.from("projects")
                    .select {
                        filter {
                            eq("id", projectID)
                        }
                        limit(1)
                    }
                    .decodeSingle<Project>()

                Result.success(project)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch project with ID $projectID", e)
                Result.failure(e)
            }
        }
}
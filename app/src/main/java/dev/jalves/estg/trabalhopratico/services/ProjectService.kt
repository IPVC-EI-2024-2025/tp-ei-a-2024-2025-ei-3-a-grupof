package dev.jalves.estg.trabalhopratico.services

import android.content.ContentValues.TAG
import android.util.Log
import dev.jalves.estg.trabalhopratico.dto.CreateProjectDTO
import dev.jalves.estg.trabalhopratico.dto.ProjectDTO
import dev.jalves.estg.trabalhopratico.dto.UpdateProjectDTO
import dev.jalves.estg.trabalhopratico.objects.EmployeeProject
import dev.jalves.estg.trabalhopratico.objects.Project
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

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


    suspend fun AddEmployeeToProject(userID:String,ProjectId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {

                val EmployeeProject = EmployeeProject(
                    userId = userID,
                    projectId = ProjectId
                )

                supabase.from("employee_project").insert(EmployeeProject)

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to Asssign Project to Employee", e)
                Result.failure(e)
            }
        }


    suspend fun removeEmployeeFromProject(userID: String, projectId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                supabase.from("employee_project")
                    .delete {
                        filter {
                            eq("user_id", userID)
                            eq("project_id", projectId)
                        }
                    }

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove employee $userID from project $projectId", e)
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

    suspend fun getProjectByID(projectID: String): Result<ProjectDTO> =
        withContext(Dispatchers.IO) {
            try {
                val project = supabase.postgrest.rpc("get_project", parameters = buildJsonObject {
                    put("p_id", projectID)
                })

                Log.d("PROJECT", project.data)

                Result.success(project.decodeAs<ProjectDTO>())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch project with ID $projectID", e)
                Result.failure(e)
            }
        }

    suspend fun updateProject(updatedProject: UpdateProjectDTO) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("PROJECT", updatedProject.id)

                supabase.from("projects").update(
                    buildMap {
                        updatedProject.name?.let { put("name", it) }
                        updatedProject.description?.let { put("description", it) }
                        updatedProject.startDate?.let { put("start_date", it) }
                        updatedProject.dueDate?.let { put("due_date", it) }
                        updatedProject.status?.let { put("status", it) }
                        updatedProject.managerID?.let { put("manager_id", it) }
                    }
                ) {
                    filter {
                        eq("id", updatedProject.id)
                    }
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update project", e)
                Result.failure(e)
            }
        }
}
package dev.jalves.estg.trabalhopratico.services

import android.content.ContentValues.TAG
import android.util.Log
import dev.jalves.estg.trabalhopratico.dto.CreateTaskAssignmentDTO
import dev.jalves.estg.trabalhopratico.dto.CreateTaskDTO
import dev.jalves.estg.trabalhopratico.dto.UpdateTask
import dev.jalves.estg.trabalhopratico.objects.EmployeeTaskAssignment
import dev.jalves.estg.trabalhopratico.objects.Task
import dev.jalves.estg.trabalhopratico.objects.TaskStatus
import dev.jalves.estg.trabalhopratico.objects.User
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object TaskService {
    suspend fun createTask(dto: CreateTaskDTO, id: String): Result<Unit> =
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
                Log.e(TAG, "Failed to create task", e)
                Result.failure(e)
            }
        }


    suspend fun assignTaskToEmployee(userID:String,taskID: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Task assigned to employee successfully")
                val taskAssignment = CreateTaskAssignmentDTO(
                    taskId = taskID,
                    employeeId = userID,
                    completionRate = 0.0f,
                )

                supabase.from("employee_task_assignments").insert(taskAssignment)

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to Asssign Task to Employee", e)
                Result.failure(e)
            }
        }

    suspend fun getTaskByID(taskId: String): Result<Task> =
        withContext(Dispatchers.IO) {
            try {
                val task = supabase.from("tasks")
                    .select {
                        filter {
                            eq("id", taskId)
                        }
                    }
                    .decodeSingle<Task>()

                Result.success(task)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch task by ID", e)
                Result.failure(e)
            }
        }

    suspend fun listTasks(): Result<List<Task>> =
        withContext(Dispatchers.IO) {
            try {
                val tasks = supabase.from("tasks")
                    .select()
                    .decodeList<Task>()

                Result.success(tasks)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch tasks", e)
                Result.failure(e)
            }
        }

    suspend fun getProjectTaskCount(projectId: String): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                val tasks = supabase.from("tasks")
                    .select {
                        filter {
                            eq("project_id", projectId)
                        }
                    }
                    .decodeList<Task>()

                Result.success(tasks.size)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch task count for project $projectId", e)
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
                val status = if (updatedTask.status == "Complete") {
                    TaskStatus.COMPLETE
                } else {
                    TaskStatus.IN_PROGRESS
                }

                val task = Task(
                    name = updatedTask.name,
                    description = updatedTask.description,
                    status = status,

                    )

                supabase.from("tasks").update(task
                ) {
                    filter {
                        eq("id", id)
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update task", e)
                Result.failure(e)
            }
        }

    suspend fun markTaskComplete(taskId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val updateData = buildJsonObject {
                    put("status", TaskStatus.COMPLETE.name)
                    put("updated_at", "now()")
                }

                supabase.from("tasks").update(updateData) {
                    filter {
                        eq("id", taskId)
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark task complete", e)
                Result.failure(e)
            }
        }

    suspend fun removeEmployeeFromTask(taskId: String, employeeId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                supabase.from("employee_task_assignments").delete {
                    filter {
                        eq("task_id", taskId)
                        eq("employee_id", employeeId)
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove employee from task", e)
                Result.failure(e)
            }
        }

    suspend fun getTaskEmployees(taskId: String): Result<List<User>> =
        withContext(Dispatchers.IO) {
            try {
                val assignments = supabase.from("employee_task_assignments")
                    .select {
                        filter {
                            eq("task_id", taskId)
                        }
                    }
                    .decodeList<EmployeeTaskAssignment>()

                val employees = mutableListOf<User>()
                assignments.forEach { assignment ->
                    try {
                        val user = UserService.fetchUserById(assignment.employeeId)
                        employees.add(user)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to fetch user ${assignment.employeeId}", e)
                    }
                }

                Result.success(employees)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch task employees", e)
                Result.failure(e)
            }
        }
}
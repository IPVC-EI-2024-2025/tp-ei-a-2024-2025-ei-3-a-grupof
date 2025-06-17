package dev.jalves.estg.trabalhopratico.services

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import dev.jalves.estg.trabalhopratico.dto.CreateTaskAssignmentDTO
import dev.jalves.estg.trabalhopratico.dto.CreateTaskDTO
import dev.jalves.estg.trabalhopratico.dto.TaskOverviewDTO
import dev.jalves.estg.trabalhopratico.dto.UpdateTask
import dev.jalves.estg.trabalhopratico.objects.EmployeeTaskAssignment
import dev.jalves.estg.trabalhopratico.objects.Task
import dev.jalves.estg.trabalhopratico.objects.TaskStatus
import dev.jalves.estg.trabalhopratico.objects.User
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    suspend fun listTasksByUser(userID: String): Result<List<Task>> =
        withContext(Dispatchers.IO) {
            try {
                val tasks = supabase.from("tasks")
                    .select(Columns.raw("id, name, description, status, created_at, employee_task_assignments!inner(employee_id)")) {
                        filter {
                            eq("employee_task_assignments.employee_id", userID)
                        }
                    }
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

    suspend fun updateTask(updatedTask: UpdateTask ) =
        withContext(Dispatchers.IO) {
            try {
                supabase.from("tasks").update(
                    buildMap {
                        updatedTask.name?.let { put("name", it) }
                        updatedTask.description?.let { put("description", it) }
                    }
                ) {
                    filter {
                        eq("id", updatedTask.id)
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
                supabase.from("tasks").update({
                    set("status", TaskStatus.COMPLETE.value)
                    set("updated_at", "now()")
                }) {
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

    suspend fun getTaskOverview(taskId: String): Result<TaskOverviewDTO> =
        withContext(Dispatchers.IO) {
            try {
                val overview = supabase.postgrest.rpc(
                    function = "get_task_overview",
                    parameters = buildJsonObject {
                        put("p_task_id", taskId)
                    }
                )

                Log.d("TASK_OVERVIEW", overview.data)

                Result.success(overview.decodeAs<TaskOverviewDTO>())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch task overview for ID $taskId", e)
                Result.failure(e)
            }
        }
    suspend fun exportTaskStatsToPDF(
        context: Context,
        taskId: String,
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val overviewResult = getTaskOverview(taskId)

            if (overviewResult.isFailure) {
                withContext(Dispatchers.Main) {
                    onError("Failed to fetch task data: ${overviewResult.exceptionOrNull()?.message}")
                }
                return@withContext
            }

            val overview = overviewResult.getOrThrow()

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 24f
                isFakeBoldText = true
            }

            val headerPaint = Paint().apply {
                color = Color.BLACK
                textSize = 18f
                isFakeBoldText = true
            }

            val bodyPaint = Paint().apply {
                color = Color.BLACK
                textSize = 14f
            }

            val linePaint = Paint().apply {
                color = Color.GRAY
                strokeWidth = 1f
            }

            var yPosition = 80f
            val leftMargin = 50f
            val lineSpacing = 25f
            var pageNumber = 1

            fun checkPageSpace(additionalHeight: Float) {
                if (yPosition + additionalHeight > 800) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDocument.startPage(newPageInfo)
                    canvas = page.canvas
                    yPosition = 80f
                }
            }

            canvas.drawText("Task Statistics Report", leftMargin, yPosition, titlePaint)
            yPosition += 40f

            canvas.drawLine(leftMargin, yPosition, 545f, yPosition, linePaint)
            yPosition += 30f

            canvas.drawText("Task Information", leftMargin, yPosition, headerPaint)
            yPosition += 30f

            canvas.drawText("Name: ${overview.task.name}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Description: ${overview.task.description}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Project: ${overview.task.project.name}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Status: ${overview.task.status?.value}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Created At: ${overview.task.createdAt}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            checkPageSpace(60f)
            canvas.drawText("Assigned Employees", leftMargin, yPosition, headerPaint)
            yPosition += 30f

            if (overview.employees?.isEmpty() == true) {
                canvas.drawText("No employees assigned.", leftMargin + 20f, yPosition, bodyPaint)
                yPosition += lineSpacing
            } else {
                overview.employees?.forEach { employee ->
                    checkPageSpace(lineSpacing * 3)
                    canvas.drawText("• ${employee.displayName} (${employee.username})", leftMargin + 20f, yPosition, bodyPaint)
                    yPosition += lineSpacing
                    canvas.drawText("  Email: ${employee.email}", leftMargin + 40f, yPosition, bodyPaint)
                    yPosition += lineSpacing
                }
            }

            yPosition += 20f
            checkPageSpace(60f)
            canvas.drawText("Task Logs", leftMargin, yPosition, headerPaint)
            yPosition += 30f

            if (overview.taskLogs?.isEmpty() == true) {
                canvas.drawText("No task logs available.", leftMargin + 20f, yPosition, bodyPaint)
                yPosition += lineSpacing
            } else {
                overview.taskLogs?.forEachIndexed { index, log ->
                    checkPageSpace(lineSpacing * 6)
                    canvas.drawText("• Log #${index + 1}", leftMargin + 20f, yPosition, bodyPaint)
                    yPosition += lineSpacing
                    canvas.drawText("  User: ${log.userName}", leftMargin + 40f, yPosition, bodyPaint)
                    yPosition += lineSpacing
                    canvas.drawText("  Date: ${log.date}", leftMargin + 40f, yPosition, bodyPaint)
                    yPosition += lineSpacing
                    canvas.drawText("  Completion: ${log.completionRate}%", leftMargin + 40f, yPosition, bodyPaint)
                    yPosition += lineSpacing
                    canvas.drawText("  Time Spent: ${log.timeSpent}", leftMargin + 40f, yPosition, bodyPaint)
                    yPosition += lineSpacing
                    canvas.drawText("  Notes: ${log.notes}", leftMargin + 40f, yPosition, bodyPaint)
                    yPosition += lineSpacing
                }
            }

            yPosition += 30f
            checkPageSpace(60f)
            canvas.drawLine(leftMargin, yPosition, 545f, yPosition, linePaint)
            yPosition += 30f

            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            canvas.drawText("Export Date: $currentDate", leftMargin, yPosition, bodyPaint)

            pdfDocument.finishPage(page)

            val fileName = "Task_stats_${overview.task.name}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }

            pdfDocument.close()

            Log.d("TaskService", "PDF exported successfully: ${file.absolutePath}")

            withContext(Dispatchers.Main) {
                onSuccess(file)
            }

        } catch (e: Exception) {
            Log.e("TaskService", "Failed to export PDF", e)
            withContext(Dispatchers.Main) {
                onError("Failed to export PDF: ${e.message}")
            }
        }
    }}
package dev.jalves.estg.trabalhopratico.ui.views

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.jalves.estg.trabalhopratico.dto.CreateTaskLogDTO
import dev.jalves.estg.trabalhopratico.objects.TaskLog
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import dev.jalves.estg.trabalhopratico.services.TaskLogService
import dev.jalves.estg.trabalhopratico.ui.components.PhotoCarousel
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskLogView(
    taskId: String,
    onNavigateBack: () -> Unit
) {
    var logDate by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var completionPercentage by remember { mutableStateOf("") }
    var timeSpent by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isLoadingPreviousLogs by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val datePickerState = rememberDatePickerState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = selectedImages + uris
    }

    LaunchedEffect(Unit) {
        val currentDateTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date())
        logDate = currentDateTime

        coroutineScope.launch {
            isLoadingPreviousLogs = true
            try {
                TaskLogService.getTaskLogsByTaskId(taskId).fold(
                    onSuccess = { taskLogs ->
                        if (taskLogs.isNotEmpty()) {
                            val sortedLogs = taskLogs.sortedWith(
                                compareByDescending<TaskLog> {
                                    try {
                                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(it.date)
                                    } catch (e: Exception) {
                                        Date(0)
                                    }
                                }.thenByDescending { it.completionRate }
                            )

                            val highestCompletionRate = sortedLogs.first().completionRate
                            completionPercentage = String.format("%.0f", highestCompletionRate * 100)
                        }
                    },
                    onFailure = { error ->
                        Log.w("NewTaskLogView", "Failed to fetch previous logs: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                Log.w("NewTaskLogView", "Exception fetching previous logs", e)
            } finally {
                isLoadingPreviousLogs = false
            }
        }
    }

    fun handleSave() {
        if (location.isBlank() || completionPercentage.isBlank() || timeSpent.isBlank() || logDate.isBlank()) {
            Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        coroutineScope.launch {
            isLoading = true
            try {
                val taskLogDto = supabase.auth.currentUserOrNull()?.id?.let {
                    CreateTaskLogDTO(
                        userId = it,
                        taskId = taskId,
                        date = logDate,
                        location = location.trim(),
                        completionRate = completionPercentage.toFloatOrNull()?.div(100f) ?: 0f,
                        timeSpent = timeSpent.toFloatOrNull() ?: 0f,
                        notes = observations.ifBlank { null }
                    )
                } ?: run {
                    Toast.makeText(context, "User authentication error", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    return@launch
                }

                TaskLogService.createTaskLog(taskLogDto).fold(
                    onSuccess = { logId ->
                        if (selectedImages.isNotEmpty()) {
                            TaskLogService.uploadLogPhotos(logId, selectedImages, context).fold(
                                onSuccess = {
                                    Toast.makeText(context, "Task log created successfully", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                },
                                onFailure = { error ->
                                    Toast.makeText(context, "Log created but failed to upload photos: ${error.message}", Toast.LENGTH_LONG).show()
                                    Log.e("NewTaskLogView", "Error uploading photos", error)
                                    isLoading = false
                                }
                            )
                        } else {
                            Toast.makeText(context, "Task log created successfully", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }
                    },
                    onFailure = { error ->
                        Toast.makeText(context, "Failed to save log: ${error.message}", Toast.LENGTH_LONG).show()
                        Log.e("NewTaskLogView", "Error creating task log", error)
                        isLoading = false
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("NewTaskLogView", "Exception in handleSave", e)
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        TopAppBar(
            title = { Text("New Task Log") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
                    .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = logDate,
                    onValueChange = { },
                    label = {
                        Text(
                            "Log date"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                Icons.Rounded.CalendarToday,
                                contentDescription = "Select date"
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = {
                        Text(
                            "Location"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = completionPercentage,
                        onValueChange = { input ->
                            val digitsOnly = input.filter { it.isDigit() }
                            val num = digitsOnly.toFloatOrNull()
                            completionPercentage = when {
                                num == null            -> ""
                                num < 0f               -> "0"
                                num > 100f             -> "100"
                                else                   -> digitsOnly
                            }
                        },
                        label = {
                            Text(
                                "Completion percentage",
                            )
                        },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        suffix = {
                            Text(
                                "%",
                            )
                        },
                        enabled = !isLoadingPreviousLogs
                    )

                    OutlinedTextField(
                        value = timeSpent,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() || it == '.' }
                            val num = filtered.toFloatOrNull()
                            timeSpent = when {
                                num == null        -> ""
                                num < 0f           -> "0"
                                else               -> filtered
                            }
                        },
                        label = {
                            Text(
                                "Time spent"
                            )
                        },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }

                OutlinedTextField(
                    value = observations,
                    onValueChange = { observations = it },
                    label = {
                        Text(
                            "Observations"
                        )
                    },
                    placeholder = {
                        Text(
                            "Add some notes..."
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 10
                )

                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Rounded.Image,
                        contentDescription = "Add Images",
                        modifier = Modifier.size(20.dp).padding(end = 8.dp)
                    )
                    Text("Add Images")
                }

                if (selectedImages.isNotEmpty()) {
                    PhotoCarousel(
                        photos = selectedImages,
                        showDeleteButton = true,
                        onDeletePhoto = { uri ->
                            selectedImages = selectedImages.filter { it != uri }
                        },
                        handlePhotoViewInternally = true
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isLoadingPreviousLogs) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Loading previous completion rate..."
                        )
                    }
                }
            }

            // Buttons anchored to the bottom
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { handleSave() },
                        enabled = !isLoading && !isLoadingPreviousLogs,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Date(millis)
                            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                            val currentTime = timeFormat.format(Date())
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            logDate = "${dateFormat.format(date)} $currentTime"
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
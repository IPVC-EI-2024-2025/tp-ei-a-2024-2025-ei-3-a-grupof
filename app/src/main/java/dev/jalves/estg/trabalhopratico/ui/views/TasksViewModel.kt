package dev.jalves.estg.trabalhopratico.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jalves.estg.trabalhopratico.objects.Task
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import dev.jalves.estg.trabalhopratico.services.TaskService
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TasksViewModel : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>?>(null)
    val tasks: StateFlow<List<Task>?> = _tasks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        fetchData()
    }

    fun fetchData() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            supabase.auth.awaitInitialization()

            val user = supabase.auth.currentUserOrNull()!!

            val result = TaskService.listTasksByUser(user.id)
            result.onSuccess { taskList ->
                _tasks.value = taskList
            }.onFailure { exception ->
                _errorMessage.value = exception.message
            }
        }
    }
}
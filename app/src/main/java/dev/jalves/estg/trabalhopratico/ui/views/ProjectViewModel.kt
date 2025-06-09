package dev.jalves.estg.trabalhopratico.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jalves.estg.trabalhopratico.objects.Project
import dev.jalves.estg.trabalhopratico.objects.TaskSyncUser
import dev.jalves.estg.trabalhopratico.services.ProjectService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProjectViewModel : ViewModel() {
    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project

    private val _manager = MutableStateFlow<TaskSyncUser?>(null)
    val manager: StateFlow<TaskSyncUser?> = _manager

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadProject(projectId: String) {
        viewModelScope.launch {
            val result = ProjectService.getProjectByID(projectId)
            result.onSuccess {
                _project.value = it
                if(_project.value!!.managerID != null) {
                    val res = ProjectService.getProjectManager(_project.value!!.managerID!!)
                    res.onSuccess {
                        _manager.value = it
                    }.onFailure {
                        _error.value = it.message
                    }
                }
            }.onFailure {
                _error.value = it.message
            }
        }
    }
}
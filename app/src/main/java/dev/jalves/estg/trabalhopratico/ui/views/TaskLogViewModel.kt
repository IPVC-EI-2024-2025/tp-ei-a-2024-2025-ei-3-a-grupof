package dev.jalves.estg.trabalhopratico.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jalves.estg.trabalhopratico.objects.TaskLog
import dev.jalves.estg.trabalhopratico.objects.LogPhotos
import dev.jalves.estg.trabalhopratico.services.TaskLogService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskLogViewModel : ViewModel() {
    private val _taskLog = MutableStateFlow<TaskLog?>(null)
    val taskLog: StateFlow<TaskLog?> = _taskLog

    private val _logPhotos = MutableStateFlow<List<LogPhotos>>(emptyList())
    val logPhotos: StateFlow<List<LogPhotos>> = _logPhotos

    private val _photoUrls = MutableStateFlow<Map<String, String>>(emptyMap())
    val photoUrls: StateFlow<Map<String, String>> = _photoUrls

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadTaskLog(logId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val logResult = TaskLogService.getTaskLogById(logId)
                logResult.fold(
                    onSuccess = { log ->
                        _taskLog.value = log
                        loadLogPhotos(logId)
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _taskLog.value = null
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message
                _taskLog.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadLogPhotos(logId: String) {
        viewModelScope.launch {
            try {
                val photosResult = TaskLogService.getLogPhotos(logId)
                photosResult.fold(
                    onSuccess = { photos ->
                        _logPhotos.value = photos
                        loadSignedUrls(photos)
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun loadSignedUrls(photos: List<LogPhotos>) {
        viewModelScope.launch {
            val urlMap = mutableMapOf<String, String>()

            photos.forEach { photo ->
                try {
                    val urlResult = TaskLogService.getSignedUrlForPhoto(photo.photoUrl)
                    urlResult.fold(
                        onSuccess = { signedUrl ->
                            urlMap[photo.photoUrl] = signedUrl
                        },
                        onFailure = { exception ->
                            _error.value = exception.message
                        }
                    )
                } catch (e: Exception) {
                    _error.value = e.message
                }
            }

            _photoUrls.value = urlMap
        }
    }
}
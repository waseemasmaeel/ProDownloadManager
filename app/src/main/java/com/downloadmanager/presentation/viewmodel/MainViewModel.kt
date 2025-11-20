package com.downloadmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.downloadmanager.data.repository.DownloadRepositoryImpl
import com.downloadmanager.domain.model.DownloadStatus
import com.downloadmanager.domain.model.DownloadTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DownloadUiState(
    val downloads: List<DownloadTask> = emptyList(),
    val activeDownloads: List<DownloadTask> = emptyList(),
    val completedDownloads: List<DownloadTask> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val urlInput: String = ""
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: DownloadRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadUiState())
    val uiState: StateFlow<DownloadUiState> = _uiState.asStateFlow()

    init {
        loadDownloads()
    }

    private fun loadDownloads() {
        viewModelScope.launch {
            repository.getAllDownloads().collect { downloads ->
                _uiState.value = _uiState.value.copy(
                    downloads = downloads,
                    activeDownloads = downloads.filter { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PENDING },
                    completedDownloads = downloads.filter { it.status == DownloadStatus.COMPLETED },
                    isLoading = false
                )
            }
        }
    }

    fun addDownload(url: String, fileName: String? = null) {
        viewModelScope.launch {
            try {
                val task = repository.createDownloadTask(url, fileName)
                repository.addDownload(task)
                startDownload(task)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun startDownload(task: DownloadTask) {
        viewModelScope.launch {
            repository.startDownload(task.id)
            repository.performDownload(task)
        }
    }

    fun pauseDownload(id: String) {
        viewModelScope.launch {
            repository.pauseDownload(id)
        }
    }

    fun resumeDownload(task: DownloadTask) {
        viewModelScope.launch {
            repository.resumeDownload(task.id)
            repository.performDownload(task)
        }
    }

    fun cancelDownload(id: String) {
        viewModelScope.launch {
            repository.cancelDownload(id)
        }
    }

    fun deleteDownload(id: String) {
        viewModelScope.launch {
            repository.deleteDownload(id)
        }
    }

    fun deleteAllDownloads() {
        viewModelScope.launch {
            repository.deleteAllDownloads()
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false, urlInput = "")
    }

    fun updateUrlInput(url: String) {
        _uiState.value = _uiState.value.copy(urlInput = url)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

package com.aubynsamuel.netstatinfo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aubynsamuel.netstatinfo.data.model.AppDataUsage
import com.aubynsamuel.netstatinfo.data.model.TimePeriod
import com.aubynsamuel.netstatinfo.data.repository.DataUsageRepository
import com.aubynsamuel.netstatinfo.data.repository.DataUsageRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DataUsageUiState {
    object Loading : DataUsageUiState
    data class Success(val apps: List<AppDataUsage>) : DataUsageUiState
    data class Error(val message: String) : DataUsageUiState
    object PermissionRequired : DataUsageUiState
}

class DataUsageViewModel(private val repository: DataUsageRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<DataUsageUiState>(DataUsageUiState.Loading)
    val uiState: StateFlow<DataUsageUiState> = _uiState.asStateFlow()

    private val _selectedTimePeriod = MutableStateFlow(TimePeriod.LAST_6_HOURS)
    val selectedTimePeriod: StateFlow<TimePeriod> = _selectedTimePeriod.asStateFlow()

    fun checkPermissionAndFetch() {
        if (!repository.hasUsageStatsPermission()) {
            _uiState.value = DataUsageUiState.PermissionRequired
        } else {
            // Force fetch if coming from permission required state, otherwise do regular fetch
            if (_uiState.value is DataUsageUiState.PermissionRequired) {
                fetchData(_selectedTimePeriod.value)
            } else {
                fetchData(_selectedTimePeriod.value)
            }
        }
    }

    fun selectTimePeriod(period: TimePeriod) {
        _selectedTimePeriod.value = period
        if (repository.hasUsageStatsPermission()) {
            fetchData(period)
        } else {
            _uiState.value = DataUsageUiState.PermissionRequired
        }
    }

    fun fetchData(period: TimePeriod = _selectedTimePeriod.value) {
        viewModelScope.launch {
            _uiState.value = DataUsageUiState.Loading
            try {
                if (!repository.hasUsageStatsPermission()) {
                    _uiState.value = DataUsageUiState.PermissionRequired
                    return@launch
                }
                
                val endTime = System.currentTimeMillis()
                val startTime = period.getStartTimeMillis()
                
                val result = repository.getAppsDataUsage(startTime, endTime)
                _uiState.value = DataUsageUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = DataUsageUiState.Error("Failed to fetch data usage: ${e.localizedMessage ?: e.message}")
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = DataUsageRepositoryImpl(context.applicationContext)
                return DataUsageViewModel(repository) as T
            }
        }
    }
}

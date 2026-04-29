package com.example.fokusflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fokusflow.network.Quote
import com.example.fokusflow.network.QuoteApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TaskViewModel(private val taskDao: TaskDao) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _quote = MutableStateFlow<Quote?>(null)
    val quote: StateFlow<Quote?> = _quote

    init {
        purgeOldTasks()
        fetchQuote()
    }

    private fun fetchQuote() {
        viewModelScope.launch {
            try {
                val quotes = QuoteApi.create().getRandomQuote()
                _quote.value = quotes.firstOrNull()
            } catch (e: Exception) {
                // V případě chyby (např. bez internetu) zůstane null
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val freeTasks: StateFlow<List<Task>> = taskDao.getFreeTasks()
        .combine(_searchQuery) { tasks, query ->
            if (query.isBlank()) tasks else tasks.filter { it.name.contains(query, ignoreCase = true) || it.description?.contains(query, ignoreCase = true) == true }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deadlineTasks: StateFlow<List<Task>> = taskDao.getDeadlineTasks()
        .combine(_searchQuery) { tasks, query ->
            if (query.isBlank()) tasks else tasks.filter { it.name.contains(query, ignoreCase = true) || it.description?.contains(query, ignoreCase = true) == true }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTasks: StateFlow<List<Task>> = taskDao.getCompletedTasks()
        .combine(_searchQuery) { tasks, query ->
            if (query.isBlank()) tasks else tasks.filter { it.name.contains(query, ignoreCase = true) || it.description?.contains(query, ignoreCase = true) == true }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedTasks: StateFlow<List<Task>> = taskDao.getDeletedTasks()
        .combine(_searchQuery) { tasks, query ->
            if (query.isBlank()) tasks else tasks.filter { it.name.contains(query, ignoreCase = true) || it.description?.contains(query, ignoreCase = true) == true }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun moveToTrash(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isDeleted = true, deletedAt = LocalDate.now()))
        }
    }

    fun restoreTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isDeleted = false, deletedAt = null))
        }
    }

    fun deletePermanently(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }

    private fun purgeOldTasks() {
        viewModelScope.launch {
            val threshold = LocalDate.now().minusDays(30)
            taskDao.purgeOldDeletedTasks(threshold)
        }
    }

    fun addTask(name: String, description: String?, priority: Priority, dueDate: LocalDate?, latitude: Double? = null, longitude: Double? = null, locationName: String? = null) {
        viewModelScope.launch {
            val newTask = Task(
                name = name,
                description = description?.takeIf { it.isNotBlank() },
                priority = priority,
                dueDate = dueDate,
                latitude = latitude,
                longitude = longitude,
                locationName = locationName
            )
            taskDao.insertTask(newTask)
        }
    }

    fun updateTask(id: Int, name: String, description: String?, priority: Priority, dueDate: LocalDate?, isCompleted: Boolean, isDeleted: Boolean, deletedAt: LocalDate?, latitude: Double? = null, longitude: Double? = null, locationName: String? = null) {
        viewModelScope.launch {
            val updatedTask = Task(
                id = id,
                name = name,
                description = description?.takeIf { it.isNotBlank() },
                priority = priority,
                dueDate = dueDate,
                isCompleted = isCompleted,
                isDeleted = isDeleted,
                deletedAt = deletedAt,
                latitude = latitude,
                longitude = longitude,
                locationName = locationName
            )
            taskDao.updateTask(updatedTask)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }
}
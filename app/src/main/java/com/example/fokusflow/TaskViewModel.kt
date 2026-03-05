package com.example.fokusflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TaskViewModel(private val taskDao: TaskDao) : ViewModel() {

    init {
        purgeOldTasks()
    }

    val freeTasks: StateFlow<List<Task>> = taskDao.getFreeTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deadlineTasks: StateFlow<List<Task>> = taskDao.getDeadlineTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTasks: StateFlow<List<Task>> = taskDao.getCompletedTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedTasks: StateFlow<List<Task>> = taskDao.getDeletedTasks()
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

    fun addTask(name: String, description: String?, priority: Priority, dueDate: LocalDate?) {
        viewModelScope.launch {
            val newTask = Task(
                name = name,
                description = description?.takeIf { it.isNotBlank() },
                priority = priority,
                dueDate = dueDate
            )
            taskDao.insertTask(newTask)
        }
    }

    fun updateTask(id: Int, name: String, description: String?, priority: Priority, dueDate: LocalDate?, isCompleted: Boolean, isDeleted: Boolean, deletedAt: LocalDate?) {
        viewModelScope.launch {
            val updatedTask = Task(
                id = id,
                name = name,
                description = description?.takeIf { it.isNotBlank() },
                priority = priority,
                dueDate = dueDate,
                isCompleted = isCompleted,
                isDeleted = isDeleted,
                deletedAt = deletedAt
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
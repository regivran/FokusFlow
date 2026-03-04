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

    // Všechny úkoly z databáze jako StateFlow
    val allTasks: StateFlow<List<Task>> = taskDao.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Odvozené seznamy pro UI
    val deadlineTasks: StateFlow<List<Task>> = allTasks.map { tasks ->
        tasks.filter { it.dueDate != null }.sortedBy { it.dueDate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val freeTasks: StateFlow<List<Task>> = allTasks.map { tasks ->
        tasks.filter { it.dueDate == null }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
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

    fun updateTask(id: Int, name: String, description: String?, priority: Priority, dueDate: LocalDate?) {
        viewModelScope.launch {
            val updatedTask = Task(
                id = id,
                name = name,
                description = description?.takeIf { it.isNotBlank() },
                priority = priority,
                dueDate = dueDate
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
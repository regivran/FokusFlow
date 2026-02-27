package com.example.fokusflow

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.time.LocalDate

class TaskViewModel : ViewModel() {
    private val _tasks = mutableStateListOf<Task>()
    val tasks: List<Task> = _tasks

    val deadlineTasks: List<Task> by derivedStateOf {
        _tasks.filter { it.dueDate != null }.sortedBy { it.dueDate }
    }

    val freeTasks: List<Task> by derivedStateOf {
        _tasks.filter { it.dueDate == null }
    }

    init {
        _tasks.addAll(
            listOf(
                Task(id = 1, name = "Koupit kávu", description = "Bez kávy to nepojede", priority = Priority.High, dueDate = LocalDate.now().plusDays(1)),
                Task(id = 2, name = "Cvičit Kotlin", description = "Aspoň 20 minut", priority = Priority.Medium),
                Task(id = 3, name = "Zalít kytky", description = null, priority = Priority.Low),
                Task(id = 4, name = "Napsat zprávu", description = "Důležitá zpráva pro šéfa", priority = Priority.High, dueDate = LocalDate.now().plusDays(3)),
                Task(id = 5, name = "Zavolat mámě", description = null, priority = Priority.Medium)
            )
        )
    }

    fun deleteTask(task: Task) {
        _tasks.remove(task)
    }

    fun addTask(name: String, description: String?, priority: Priority, dueDate: LocalDate?) {
        val newId = (_tasks.maxOfOrNull { it.id } ?: 0) + 1
        _tasks.add(Task(newId, name, description?.takeIf { it.isNotBlank() }, dueDate, priority))
    }

    fun updateTask(id: Int, name: String, description: String?, priority: Priority, dueDate: LocalDate?) {
        val index = _tasks.indexOfFirst { it.id == id }
        if (index != -1) {
            _tasks[index] = _tasks[index].copy(
                name = name,
                description = description?.takeIf { it.isNotBlank() },
                priority = priority,
                dueDate = dueDate
            )
        }
    }

    fun toggleTaskCompletion(task: Task) {
        val index = _tasks.indexOfFirst { it.id == task.id }
        if (index != -1) {
            _tasks[index] = _tasks[index].copy(isCompleted = !task.isCompleted)
        }
    }
}
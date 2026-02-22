package com.example.fokusflow

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

data class Task(
    val id: Int,
    val name: String,
    val description: String? = null,
    val dueDate: LocalDate? = null,
    val priority: Priority = Priority.Medium,
    val isCompleted: Boolean = false
)

enum class Priority(val color: Color) {
    Low(Color.Green), Medium(Color.Yellow), High(Color.Red)
}
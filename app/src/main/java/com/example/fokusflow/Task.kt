package com.example.fokusflow

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.ui.graphics.Color
import java.time.LocalDate

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String? = null,
    val dueDate: LocalDate? = null,
    val priority: Priority = Priority.Medium,
    val isCompleted: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: LocalDate? = null
)

enum class Priority(val color: Color) {
    Low(Color.Green), Medium(Color.Yellow), High(Color.Red)
}
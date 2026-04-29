package com.example.fokusflow.ui.screens

import androidx.compose.runtime.Composable
import com.example.fokusflow.Task
import com.example.fokusflow.TaskViewModel
import com.example.fokusflow.ui.components.TaskList

@Composable
fun CompletedScreen(
    viewModel: TaskViewModel,
    tasks: List<Task>,
    onEdit: (Task) -> Unit,
    onToggleCompletion: (Task) -> Unit
) {
    TaskList(
        tasks = tasks,
        onDelete = { task -> viewModel.moveToTrash(task) },
        onEdit = onEdit,
        onToggleCompletion = onToggleCompletion
    )
}

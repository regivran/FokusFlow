package com.example.fokusflow.ui.screens

import androidx.compose.runtime.Composable
import com.example.fokusflow.Task
import com.example.fokusflow.TaskViewModel
import com.example.fokusflow.ui.components.TaskList

@Composable
fun TrashScreen(
    viewModel: TaskViewModel,
    tasks: List<Task>,
    onDeletePermanently: (Task) -> Unit
) {
    TaskList(
        tasks = tasks,
        onDelete = onDeletePermanently,
        onEdit = { task -> viewModel.restoreTask(task) },
        onToggleCompletion = { },
        isTrash = true
    )
}

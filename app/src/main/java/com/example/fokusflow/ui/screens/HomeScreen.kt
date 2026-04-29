package com.example.fokusflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.fokusflow.Task
import com.example.fokusflow.TaskViewModel
import com.example.fokusflow.ui.components.TaskList

@Composable
fun HomeScreen(
    viewModel: TaskViewModel,
    freeTasks: List<Task>,
    deadlineTasks: List<Task>,
    onEdit: (Task) -> Unit,
    onToggleCompletion: (Task) -> Unit
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val titles = listOf("Volné", "S termínem")
    val searchQuery by viewModel.searchQuery.collectAsState()
    val quote by viewModel.quote.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Citát dne
        quote?.let {
            Card(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "\"${it.text}\"", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "- ${it.author}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(androidx.compose.ui.Alignment.End))
                }
            }
        }

        // Vyhledávání
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text("Hledat úkoly...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        TabRow(selectedTabIndex = selectedTabIndex) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> TaskList(
                tasks = freeTasks,
                onDelete = { task -> viewModel.moveToTrash(task) },
                onEdit = onEdit,
                onToggleCompletion = onToggleCompletion
            )
            1 -> TaskList(
                tasks = deadlineTasks,
                onDelete = { task -> viewModel.moveToTrash(task) },
                onEdit = onEdit,
                onToggleCompletion = onToggleCompletion
            )
        }
    }
}

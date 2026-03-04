package com.example.fokusflow

import androidx.compose.material3.AlertDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fokusflow.ui.theme.FokusFlowTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FokusFlowTheme {
                val context = LocalContext.current
                val database = remember { TaskDatabase.getDatabase(context) }
                val dao = remember { database.taskDao() }
                val viewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(dao))
                
                // Převod StateFlow na Compose State
                val freeTasks by viewModel.freeTasks.collectAsState()
                val deadlineTasks by viewModel.deadlineTasks.collectAsState()

                var taskToDelete by remember { mutableStateOf<Task?>(null) }
                var showAddTaskDialog by remember { mutableStateOf(false) }
                var taskToEdit by remember { mutableStateOf<Task?>(null) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showAddTaskDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Přidat úkol")
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        // Levý sloupec pro volné úkoly
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Volné úkoly",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
                            )
                            TaskList(
                                tasks = freeTasks,
                                onDelete = { task -> taskToDelete = task },
                                onEdit = { task -> taskToEdit = task },
                                onToggleCompletion = { task -> viewModel.toggleTaskCompletion(task) }
                            )
                        }

                        // Pravý sloupec pro úkoly s termínem
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Úkoly s termínem",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
                            )
                            TaskList(
                                tasks = deadlineTasks,
                                onDelete = { task -> taskToDelete = task },
                                onEdit = { task -> taskToEdit = task },
                                onToggleCompletion = { task -> viewModel.toggleTaskCompletion(task) }
                            )
                        }
                    }

                    // Dialogy
                    if (taskToDelete != null) {
                        DeleteConfirmationDialog(
                            task = taskToDelete!!,
                            onConfirm = {
                                viewModel.deleteTask(it)
                                taskToDelete = null
                            },
                            onDismiss = { taskToDelete = null }
                        )
                    }

                    if (showAddTaskDialog || taskToEdit != null) {
                        TaskDialog(
                            task = taskToEdit,
                            onConfirm = { name, description, priority, dueDate ->
                                if (taskToEdit != null) {
                                    viewModel.updateTask(taskToEdit!!.id, name, description, priority, dueDate)
                                    taskToEdit = null
                                } else {
                                    viewModel.addTask(name, description, priority, dueDate)
                                    showAddTaskDialog = false
                                }
                            },
                            onDismiss = {
                                showAddTaskDialog = false
                                taskToEdit = null
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(task: Task, onConfirm: (Task) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Smazat úkol") },
        text = { Text("Opravdu chcete smazat úkol \"${task.name}\"?") },
        confirmButton = {
            TextButton(onClick = { onConfirm(task) }) {
                Text("Smazat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušit")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    task: Task? = null,
    onConfirm: (String, String, Priority, LocalDate?) -> Unit,
    onDismiss: () -> Unit
) {
    var taskName by remember { mutableStateOf(task?.name ?: "") }
    var taskDescription by remember { mutableStateOf(task?.description ?: "") }
    var selectedPriority by remember { mutableStateOf(task?.priority ?: Priority.Medium) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(task?.dueDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    val isNameValid = taskName.isNotBlank()

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Zrušit")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (task == null) "Přidat nový úkol" else "Upravit úkol") },
        text = {
            Column {
                TextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("Název úkolu") },
                    isError = !isNameValid,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.size(8.dp))
                TextField(
                    value = taskDescription,
                    onValueChange = { taskDescription = it },
                    label = { Text("Popis (volitelné)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text("Priorita:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Priority.entries.forEach { priority ->
                        FilterChip(
                            selected = (priority == selectedPriority),
                            onClick = { selectedPriority = priority },
                            label = { Text(priority.name) }
                        )
                    }
                }
                Spacer(modifier = Modifier.size(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Termín: ", style = MaterialTheme.typography.labelMedium)
                    Text(selectedDate?.toString() ?: "Žádný")
                    Spacer(modifier = Modifier.weight(1f))
                    if(selectedDate != null){
                        IconButton(onClick = { selectedDate = null }) {
                           Icon(Icons.Default.Delete, contentDescription = "Smazat termín")
                        }
                    }
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Vybrat termín")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(taskName, taskDescription, selectedPriority, selectedDate) },
                enabled = isNameValid
            ) {
                Text(if (task == null) "Přidat" else "Uložit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušit")
            }
        }
    )
}

@Composable
fun TaskList(tasks: List<Task>, onDelete: (Task) -> Unit, onEdit: (Task) -> Unit, onToggleCompletion: (Task) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(
            items = tasks,
            key = { task -> task.id }
        ) { task ->
            TaskItem(
                task = task,
                onDelete = { onDelete(task) },
                onEdit = { onEdit(task) },
                onToggleCompletion = { onToggleCompletion(task) }
            )
        }
    }
}

@Composable
fun TaskItem(task: Task, onDelete: () -> Unit, onEdit: () -> Unit, onToggleCompletion: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .alpha(if (task.isCompleted) 0.6f else 1f),
        colors = CardDefaults.cardColors(
            containerColor = task.priority.color.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleCompletion() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (!task.description.isNullOrEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            }
            Column {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Upravit", tint = Color.DarkGray)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Smazat", tint = Color.DarkGray)
                }
            }
        }
    }
}
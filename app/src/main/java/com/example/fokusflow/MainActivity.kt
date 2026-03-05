package com.example.fokusflow

import androidx.compose.material3.AlertDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fokusflow.ui.theme.FokusFlowTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext

import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.mutableIntStateOf

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
                
                val freeTasks by viewModel.freeTasks.collectAsState()
                val deadlineTasks by viewModel.deadlineTasks.collectAsState()
                val completedTasks by viewModel.completedTasks.collectAsState()
                val deletedTasks by viewModel.deletedTasks.collectAsState()

                var currentView by rememberSaveable { mutableStateOf("home") }
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
                val titles = listOf("Volné", "S termínem")

                var taskToDelete by remember { mutableStateOf<Task?>(null) }
                var showAddTaskDialog by remember { mutableStateOf(false) }
                var taskToEdit by remember { mutableStateOf<Task?>(null) }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Text("FokusFlow", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineMedium)
                            HorizontalDivider()
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                label = { Text("Domů") },
                                selected = currentView == "home",
                                onClick = {
                                    currentView = "home"
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                                label = { Text("Hotové") },
                                selected = currentView == "completed",
                                onClick = {
                                    currentView = "completed"
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                label = { Text("Koš") },
                                selected = currentView == "trash",
                                onClick = {
                                    currentView = "trash"
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = { 
                                    Text(when(currentView) {
                                        "home" -> "Moje úkoly"
                                        "completed" -> "Hotové úkoly"
                                        "trash" -> "Koš"
                                        else -> "FokusFlow"
                                    })
                                },
                                navigationIcon = {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                                    }
                                }
                            )
                        },
                        floatingActionButton = {
                            if (currentView == "home") {
                                FloatingActionButton(onClick = { showAddTaskDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = "Přidat úkol")
                                }
                            }
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                        ) {
                            when (currentView) {
                                "home" -> {
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
                                            onEdit = { task -> taskToEdit = task },
                                            onToggleCompletion = { task -> viewModel.toggleTaskCompletion(task) }
                                        )
                                        1 -> TaskList(
                                            tasks = deadlineTasks,
                                            onDelete = { task -> viewModel.moveToTrash(task) },
                                            onEdit = { task -> taskToEdit = task },
                                            onToggleCompletion = { task -> viewModel.toggleTaskCompletion(task) }
                                        )
                                    }
                                }
                                "completed" -> {
                                    TaskList(
                                        tasks = completedTasks,
                                        onDelete = { task -> viewModel.moveToTrash(task) },
                                        onEdit = { task -> taskToEdit = task },
                                        onToggleCompletion = { task -> viewModel.toggleTaskCompletion(task) }
                                    )
                                }
                                "trash" -> {
                                    TaskList(
                                        tasks = deletedTasks,
                                        onDelete = { task -> taskToDelete = task },
                                        onEdit = { task -> viewModel.restoreTask(task) }, // V koši Edit = Obnovit
                                        onToggleCompletion = { /* V koši nejde odškrtnout */ },
                                        isTrash = true
                                    )
                                }
                            }
                        }

                        // Dialogy
                        if (taskToDelete != null) {
                            DeleteConfirmationDialog(
                                task = taskToDelete!!,
                                onConfirm = {
                                    viewModel.deletePermanently(it)
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
                                        viewModel.updateTask(
                                            taskToEdit!!.id, name, description, priority, dueDate,
                                            taskToEdit!!.isCompleted, taskToEdit!!.isDeleted, taskToEdit!!.deletedAt
                                        )
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
fun TaskList(
    tasks: List<Task>,
    onDelete: (Task) -> Unit,
    onEdit: (Task) -> Unit,
    onToggleCompletion: (Task) -> Unit,
    isTrash: Boolean = false
) {
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
                onToggleCompletion = { onToggleCompletion(task) },
                isTrash = isTrash
            )
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onToggleCompletion: () -> Unit,
    isTrash: Boolean = false
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("d. M. yyyy") }
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .alpha(if (task.isCompleted && !isTrash) 0.6f else 1f)
            .clickable { isExpanded = !isExpanded }
            .animateContentSize(),
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
            if (!isTrash) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleCompletion() }
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isCompleted && !isTrash) TextDecoration.LineThrough else TextDecoration.None
                )
                if (!task.description.isNullOrEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = if (task.isCompleted && !isTrash) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Zobrazení data, pokud existuje
                if (task.dueDate != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = task.dueDate.format(dateFormatter),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
                if (isTrash && task.deletedAt != null) {
                    Text(
                        text = "Smazáno: ${task.deletedAt.format(dateFormatter)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Column {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (isTrash) Icons.Default.Refresh else Icons.Default.Edit,
                        contentDescription = if (isTrash) "Obnovit" else "Upravit",
                        tint = Color.DarkGray
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Smazat", tint = Color.DarkGray)
                }
            }
        }
    }
}
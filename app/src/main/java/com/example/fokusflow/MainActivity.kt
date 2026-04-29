package com.example.fokusflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fokusflow.ui.theme.FokusFlowTheme
import com.example.fokusflow.ui.components.*
import com.example.fokusflow.ui.screens.*
import kotlinx.coroutines.launch

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
                                onClick = { currentView = "home"; scope.launch { drawerState.close() } }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                                label = { Text("Hotové") },
                                selected = currentView == "completed",
                                onClick = { currentView = "completed"; scope.launch { drawerState.close() } }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                label = { Text("Koš") },
                                selected = currentView == "trash",
                                onClick = { currentView = "trash"; scope.launch { drawerState.close() } }
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
                        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                            when (currentView) {
                                "home" -> HomeScreen(
                                    viewModel = viewModel,
                                    freeTasks = freeTasks,
                                    deadlineTasks = deadlineTasks,
                                    onEdit = { taskToEdit = it },
                                    onToggleCompletion = { viewModel.toggleTaskCompletion(it) }
                                )
                                "completed" -> CompletedScreen(
                                    viewModel = viewModel,
                                    tasks = completedTasks,
                                    onEdit = { taskToEdit = it },
                                    onToggleCompletion = { viewModel.toggleTaskCompletion(it) }
                                )
                                "trash" -> TrashScreen(
                                    viewModel = viewModel,
                                    tasks = deletedTasks,
                                    onDeletePermanently = { taskToDelete = it }
                                )
                            }
                        }

                        if (taskToDelete != null) {
                            DeleteConfirmationDialog(
                                task = taskToDelete!!,
                                onConfirm = { viewModel.deletePermanently(it); taskToDelete = null },
                                onDismiss = { taskToDelete = null }
                            )
                        }

                        if (showAddTaskDialog || taskToEdit != null) {
                            TaskDialog(
                                task = taskToEdit,
                                onConfirm = { name, desc, priority, date, lat, lon, locName ->
                                    if (taskToEdit != null) {
                                        viewModel.updateTask(taskToEdit!!.id, name, desc, priority, date, taskToEdit!!.isCompleted, taskToEdit!!.isDeleted, taskToEdit!!.deletedAt, lat, lon, locName)
                                        taskToEdit = null
                                    } else {
                                        viewModel.addTask(name, desc, priority, date, lat, lon, locName)
                                        showAddTaskDialog = false
                                    }
                                },
                                onDismiss = { showAddTaskDialog = false; taskToEdit = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

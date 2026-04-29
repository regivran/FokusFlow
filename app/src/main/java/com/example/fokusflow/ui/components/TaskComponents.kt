package com.example.fokusflow.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fokusflow.Priority
import com.example.fokusflow.Task
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.app.ActivityCompat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

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
                if (task.dueDate != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(task.dueDate.format(dateFormatter), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
                if (!task.locationName.isNullOrEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(task.locationName, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
                if (isTrash && task.deletedAt != null) {
                    Text("Smazáno: ${task.deletedAt.format(dateFormatter)}", style = MaterialTheme.typography.labelSmall, color = Color.Red.copy(alpha = 0.7f), modifier = Modifier.padding(top = 4.dp))
                }
            }
            Column {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(if (isTrash) Icons.Default.Refresh else Icons.Default.Edit, contentDescription = null, tint = Color.DarkGray)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.DarkGray)
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
        confirmButton = { TextButton(onClick = { onConfirm(task) }) { Text("Smazat") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Zrušit") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    task: Task? = null,
    onConfirm: (String, String, Priority, LocalDate?, Double?, Double?, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var taskName by remember { mutableStateOf(task?.name ?: "") }
    var taskDescription by remember { mutableStateOf(task?.description ?: "") }
    var selectedPriority by remember { mutableStateOf(task?.priority ?: Priority.Medium) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(task?.dueDate) }
    var latitude by remember { mutableStateOf(task?.latitude) }
    var longitude by remember { mutableStateOf(task?.longitude) }
    var locationName by remember { mutableStateOf(task?.locationName) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    val isNameValid = taskName.isNotBlank()

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Zrušit") } }
        ) { DatePicker(state = datePickerState) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (task == null) "Přidat nový úkol" else "Upravit úkol") },
        text = {
            Column {
                TextField(value = taskName, onValueChange = { taskName = it }, label = { Text("Název úkolu") }, isError = !isNameValid, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.size(8.dp))
                TextField(value = taskDescription, onValueChange = { taskDescription = it }, label = { Text("Popis (volitelné)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.size(16.dp))
                Text("Priorita:", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Priority.entries.forEach { priority ->
                        FilterChip(selected = (priority == selectedPriority), onClick = { selectedPriority = priority }, label = { Text(priority.name) })
                    }
                }
                Spacer(modifier = Modifier.size(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Termín: ", style = MaterialTheme.typography.labelMedium)
                    Text(selectedDate?.toString() ?: "Žádný")
                    Spacer(modifier = Modifier.weight(1f))
                    if(selectedDate != null){
                        IconButton(onClick = { selectedDate = null }) { Icon(Icons.Default.Delete, contentDescription = null) }
                    }
                    IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, contentDescription = null) }
                }
                Spacer(modifier = Modifier.size(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Poloha: ", style = MaterialTheme.typography.labelMedium)
                    Text(locationName ?: "Žádná")
                    Spacer(modifier = Modifier.weight(1f))
                    if(locationName != null){
                        IconButton(onClick = { latitude = null; longitude = null; locationName = null }) { Icon(Icons.Default.Delete, contentDescription = null) }
                    }
                    IconButton(onClick = { 
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                if (location != null) {
                                    latitude = location.latitude
                                    longitude = location.longitude
                                    try {
                                        val geocoder = Geocoder(context, Locale.getDefault())
                                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                        locationName = addresses?.firstOrNull()?.locality ?: "Neznámé místo"
                                    } catch (e: Exception) { locationName = "Neznámé místo" }
                                }
                            }
                        }
                    }) { Icon(Icons.Default.LocationOn, contentDescription = null) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(taskName, taskDescription, selectedPriority, selectedDate, latitude, longitude, locationName) }, enabled = isNameValid) {
                Text(if (task == null) "Přidat" else "Uložit")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Zrušit") } }
    )
}

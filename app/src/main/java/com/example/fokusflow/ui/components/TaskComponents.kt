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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.fokusflow.Priority
import com.example.fokusflow.Task
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn, 
                            contentDescription = "Poloha", 
                            modifier = Modifier.size(16.dp), 
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = task.locationName, 
                            style = MaterialTheme.typography.labelMedium, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
    var isFetchingLocation by remember { mutableStateOf(false) }
    val isNameValid = taskName.isNotBlank()

    val fetchLocation = {
        isFetchingLocation = true
        locationName = "Čekám na GPS signál..."
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Vynutíme nejvyšší možnou přesnost a budeme čekat na čerstvý výsledek
            fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { freshLocation ->
                    // Filtrujeme nepřesné výsledky (typicky ty Semily ze sítě mají přesnost v kilometrech)
                    if (freshLocation != null && freshLocation.accuracy < 200) {
                        processLocation(freshLocation, context) { lat, lon, name ->
                            latitude = lat
                            longitude = lon
                            locationName = name
                            isFetchingLocation = false
                        }
                    } else if (freshLocation != null) {
                        locationName = "Nízká přesnost (${freshLocation.accuracy.toInt()}m)"
                        isFetchingLocation = false
                    } else {
                        locationName = "Slabý GPS signál"
                        isFetchingLocation = false
                    }
                }
                .addOnFailureListener {
                    locationName = "Chyba GPS senzoru"
                    isFetchingLocation = false
                }
        } else {
            isFetchingLocation = false
            locationName = "Chybí oprávnění GPS"
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            fetchLocation()
        }
    }

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
                    Text(
                        text = locationName ?: "Žádná", 
                        color = if (isFetchingLocation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if(locationName != null && !isFetchingLocation){
                        IconButton(onClick = { latitude = null; longitude = null; locationName = null }) { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    }
                    if (isFetchingLocation) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = { 
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                            } else {
                                fetchLocation()
                            }
                        }) { Icon(Icons.Default.LocationOn, contentDescription = null) }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(taskName, taskDescription, selectedPriority, selectedDate, latitude, longitude, locationName) }, enabled = isNameValid && !isFetchingLocation) {
                Text(if (task == null) "Přidat" else "Uložit")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Zrušit") } }
    )
}

private fun processLocation(location: android.location.Location, context: android.content.Context, onResult: (Double, Double, String) -> Unit) {
    val lat = location.latitude
    val lon = location.longitude
    var name = "Souřadnice: ${String.format("%.4f", lat)}, ${String.format("%.4f", lon)}"
    try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, lon, 5)
        
        if (!addresses.isNullOrEmpty()) {
            // Prioritně hledáme nejpřesnější název (locality nesmí být okresní město pokud jsme jinde)
            for (addr in addresses) {
                val locality = addr.locality
                val subLocality = addr.subLocality
                val subAdmin = addr.subAdminArea
                
                // Pokud locality není null a není to jen název okresu, použijeme ji
                if (locality != null && locality != subAdmin) {
                    name = locality
                    break
                } else if (subLocality != null) {
                    name = subLocality
                    break
                } else if (locality != null) {
                    name = locality
                    break
                }
            }
        }
    } catch (e: Exception) {
        // Ponecháme souřadnice
    }
    onResult(lat, lon, name)
}

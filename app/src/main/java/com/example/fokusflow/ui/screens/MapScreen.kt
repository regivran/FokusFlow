package com.example.fokusflow.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.fokusflow.Task
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(50.0755, 14.4378), 7f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        tasks.filter { it.latitude != null && it.longitude != null }.forEach { task ->
            Marker(
                state = MarkerState(position = LatLng(task.latitude!!, task.longitude!!)),
                title = task.name,
                snippet = task.description,
                onInfoWindowClick = { onTaskClick(task) }
            )
        }
    }
}

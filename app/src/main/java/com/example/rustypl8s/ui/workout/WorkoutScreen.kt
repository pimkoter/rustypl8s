package com.example.rustypl8s.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rustypl8s.ui.components.EmptyWorkoutState
import com.example.rustypl8s.ui.components.ErrorBanner
import com.example.rustypl8s.ui.components.ExerciseCard
import com.example.rustypl8s.ui.components.ShimmerPlaceholder
import com.example.rustypl8s.ui.theme.HevyBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "Workout", 
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState is WorkoutUiState.Success) {
                            Text(
                                (uiState as WorkoutUiState.Success).session.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (uiState is WorkoutUiState.Success) {
                        TextButton(
                            onClick = { /* Finish workout */ },
                            colors = ButtonDefaults.textButtonColors(contentColor = HevyBlue)
                        ) {
                            Text("FINISH", fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is WorkoutUiState.Idle -> {
                    EmptyWorkoutState(onStart = { viewModel.startWorkout("Leg Day Intensity") })
                }
                is WorkoutUiState.Loading -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        repeat(3) {
                            ShimmerPlaceholder(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
                is WorkoutUiState.Success -> {
                    Column {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(state.session.exercise_blocks) { block ->
                                ExerciseCard(
                                    block = block,
                                    onLogSet = { id, weight, reps, type ->
                                        viewModel.logSet(id, weight, reps, null, type)
                                    }
                                )
                                Divider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    thickness = 8.dp,
                                    color = MaterialTheme.colorScheme.background
                                )
                            }
                            
                            item {
                                AddExerciseButton(onAdd = { viewModel.addExercise("bench-press-id") })
                            }
                        }
                    }
                }
                is WorkoutUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ErrorBanner(message = state.message)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.startWorkout("Retry Session") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = HevyBlue)
                        ) {
                            Text("RETRY")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddExerciseButton(onAdd: () -> Unit) {
    OutlinedButton(
        onClick = onAdd,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = HevyBlue),
        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(HevyBlue))
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("ADD EXERCISE", fontWeight = FontWeight.Bold)
    }
}

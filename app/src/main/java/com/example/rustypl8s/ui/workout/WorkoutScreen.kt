package com.example.rustypl8s.ui.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rustypl8s.ui.components.EmptyWorkoutState
import com.example.rustypl8s.ui.components.ErrorBanner
import com.example.rustypl8s.ui.components.ExerciseCard
import com.example.rustypl8s.ui.components.ShimmerPlaceholder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "RUSTY PL8S", 
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            if (uiState is WorkoutUiState.Success) {
                FloatingActionButton(
                    onClick = { viewModel.addExercise("bench-press-id") }, // Hardcoded for demo
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Exercise")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            when (val state = uiState) {
                is WorkoutUiState.Idle -> {
                    EmptyWorkoutState(onStart = { viewModel.startWorkout("Leg Day Intensity") })
                }
                is WorkoutUiState.Loading -> {
                    repeat(5) {
                        ShimmerPlaceholder(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
                is WorkoutUiState.Success -> {
                    Text(
                        text = state.session.name.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(state.session.exercise_blocks) { block ->
                            ExerciseCard(block)
                        }
                    }
                }
                is WorkoutUiState.Error -> {
                    ErrorBanner(message = state.message)
                    Button(
                        onClick = { viewModel.startWorkout("Retry Session") },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Text("RETRY")
                    }
                }
            }
        }
    }
}

package com.example.rustypl8s.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rustypl8s.ExerciseBlock
import com.example.rustypl8s.SetLog

@Composable
fun ExerciseCard(
    block: ExerciseBlock,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Exercise: ${block.data.exercise_id.take(8)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            block.data.sets.forEach { set ->
                SetRow(set)
            }
            if (block.data.sets.isEmpty()) {
                Text(
                    text = "No sets logged yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun SetRow(set: SetLog) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Set ${set.sequence_order + 1}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "${set.weight} kg x ${set.reps}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = set.set_type.name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun EmptyWorkoutState(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ready to lift?",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("START NEW SESSION")
        }
    }
}

@Composable
fun ErrorBanner(message: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

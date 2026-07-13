package com.example.rustypl8s.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rustypl8s.ExerciseBlock
import com.example.rustypl8s.SetLog
import com.example.rustypl8s.ui.theme.HevyBlue
import com.example.rustypl8s.ui.theme.SuccessGreen
import androidx.compose.ui.tooling.preview.Preview
import com.example.rustypl8s.SetType
import com.example.rustypl8s.ActiveExercise
import java.util.UUID

@Preview(showBackground = true)
@Composable
fun ExerciseCardPreview() {
    MaterialTheme {
        ExerciseCard(
            block = ExerciseBlock(
                type = "Standard",
                data = ActiveExercise(
                    exercise_id = "bench-press-id",
                    sequence_order = 0,
                    sets = listOf(
                        SetLog(UUID.randomUUID().toString(), 0, 225.0, 5, 8.5, SetType.Working, true),
                        SetLog(UUID.randomUUID().toString(), 1, 225.0, 5, 9.0, SetType.Working, false)
                    ),
                    notes = "Keep elbows tucked"
                )
            )
        )
    }
}

@Composable
fun ExerciseCard(
    block: ExerciseBlock,
    modifier: Modifier = Modifier,
    onLogSet: (String, Double, Int, String) -> Unit = { _, _, _, _ -> }
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Exercise Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Exercise", // Should be mapped from exercise ID
                style = MaterialTheme.typography.titleLarge,
                color = HevyBlue,
                fontWeight = FontWeight.Bold
            )
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Notes Placeholder
        Text(
            text = block.data.notes ?: "Add notes...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Set Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderText("SET", weight = 0.1f)
            TableHeaderText("PREVIOUS", weight = 0.3f)
            TableHeaderText("LBS", weight = 0.2f)
            TableHeaderText("REPS", weight = 0.2f)
            TableHeaderText("", weight = 0.15f) // Checkmark column
        }

        // Set Rows
        block.data.sets.forEach { set ->
            SetRow(set) { weight, reps, completed ->
                if (completed) {
                    onLogSet(
                        block.data.exercise_id, // This should actually be active_exercise_id if available
                        weight,
                        reps,
                        "Working"
                    )
                }
            }
        }

        // Add Set Button
        Button(
            onClick = { /* TODO: Call viewModel.addSet */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("+ ADD SET", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TableHeaderText(text: String, weight: Float) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
fun SetRow(
    set: SetLog,
    onLogChange: (Double, Int, Boolean) -> Unit
) {
    val backgroundColor = if (set.is_completed) SuccessGreen.copy(alpha = 0.2f) else Color.Transparent
    var weightText by remember { mutableStateOf(set.weight.toString()) }
    var repsText by remember { mutableStateOf(set.reps.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Set Number
        Text(
            text = "${set.sequence_order + 1}",
            modifier = Modifier.weight(0.1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )

        // Previous
        Text(
            text = "---", 
            modifier = Modifier.weight(0.3f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Weight Input
        Box(modifier = Modifier.weight(0.2f)) {
            EditableValue(
                value = weightText,
                onValueChange = { weightText = it }
            )
        }

        // Reps Input
        Box(modifier = Modifier.weight(0.2f)) {
            EditableValue(
                value = repsText,
                onValueChange = { repsText = it }
            )
        }

        // Checkmark Button
        Box(
            modifier = Modifier
                .weight(0.15f)
                .padding(4.dp)
                .aspectRatio(1.2f)
                .background(
                    if (set.is_completed) SuccessGreen else MaterialTheme.colorScheme.secondaryContainer,
                    RoundedCornerShape(4.dp)
                )
                .clickable { 
                    val w = weightText.toDoubleOrNull() ?: 0.0
                    val r = repsText.toIntOrNull() ?: 0
                    onLogChange(w, r, !set.is_completed)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Complete",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun EditableValue(value: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true
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
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HevyBlue)
        ) {
            Text("START NEW SESSION", modifier = Modifier.padding(vertical = 8.dp))
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

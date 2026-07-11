package com.example.rustypl8s

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.rustypl8s.ui.theme.RustyPl8sTheme
import com.example.rustypl8s.ui.workout.WorkoutScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RustyPl8sTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    WorkoutScreen()
                }
            }
        }
    }
}

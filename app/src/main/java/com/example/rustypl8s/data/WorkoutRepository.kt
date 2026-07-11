package com.example.rustypl8s.data

import com.example.rustypl8s.WorkoutSession
import com.example.rustypl8s.ExerciseBlock
import com.example.rustypl8s.SetLog
import kotlinx.serialization.json.Json
import uniffi.rustypl8s_core.WorkoutEngine
import uniffi.rustypl8s_core.WorkoutException

class WorkoutRepository(private val engine: WorkoutEngine) {
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }

    fun startWorkout(name: String): Result<WorkoutSession> = runCatching {
        val sessionJson = engine.startWorkoutSession(name, null)
        json.decodeFromString<WorkoutSession>(sessionJson)
    }

    fun getSession(sessionId: String): Result<WorkoutSession> = runCatching {
        val sessionJson = engine.getWorkoutSession(sessionId)
        json.decodeFromString<WorkoutSession>(sessionJson)
    }

    fun addExercise(sessionId: String, exerciseId: String): Result<ExerciseBlock> = runCatching {
        val blockJson = engine.addExerciseBlockToSession(sessionId, exerciseId, "Standard")
        json.decodeFromString<ExerciseBlock>(blockJson)
    }

    fun logSet(
        activeExerciseId: String, 
        weight: Double, 
        reps: Int, 
        rpe: Double?, 
        setType: String
    ): Result<SetLog> = runCatching {
        val setJson = engine.logSet(activeExerciseId, weight, reps.toUInt(), rpe, setType)
        json.decodeFromString<SetLog>(setJson)
    }

    fun getAllExercises(): Result<String> = runCatching {
        engine.getAllExercises()
    }
}

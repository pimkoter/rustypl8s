package com.example.rustypl8s

import kotlinx.serialization.Serializable

@Serializable
enum class SetType { WarmUp, Working, DropSet, MyoRep, Failure }

@Serializable
enum class EquipmentType { Barbell, Dumbbell, Machine, Cable, Bodyweight, Band }

@Serializable
enum class MuscleGroup { Chest, Back, Shoulders, Quads, Hamstrings, Glutes, Calves, Biceps, Triceps, Core }

@Serializable
data class SetLog(
    val id: String,
    val sequence_order: Int,
    val weight: Double,
    val reps: Int,
    val rpe: Double? = null,
    val set_type: SetType,
    val is_completed: Boolean
)

@Serializable
data class ActiveExercise(
    val exercise_id: String,
    val sequence_order: Int,
    val sets: List<SetLog>,
    val notes: String? = null,
    val target_rest_time: Int? = null
)

@Serializable
data class ExerciseBlock(
    val type: String,
    val data: ActiveExercise // Simplification: assuming Standard for UI prototype
)

@Serializable
data class WorkoutSession(
    val id: String,
    val template_id: String? = null,
    val name: String,
    val start_time: Long,
    val end_time: Long? = null,
    val exercise_blocks: List<ExerciseBlock>,
    val overall_notes: String? = null
)

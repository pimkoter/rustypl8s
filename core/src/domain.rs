//! Domain models for the Workout Tracker.
//!
//! These models represent the core business logic and are designed to be
//! stored in a relational SQLite database using `rusqlite`.

use serde::{Deserialize, Serialize};
use uuid::Uuid;

/// Represents the type of set performed during an exercise.
#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, Eq, Copy)]
pub enum SetType {
    /// Standard warm-up set.
    WarmUp,
    /// Primary working set.
    Working,
    /// A set performed with reduced weight immediately after a working set.
    DropSet,
    /// Myo-rep set (rest-pause variation).
    MyoRep,
    /// A set taken to absolute muscular failure.
    Failure,
}

/// Categorizes equipment used for exercises.
#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, Eq, Copy)]
pub enum EquipmentType {
    Barbell,
    Dumbbell,
    Machine,
    Cable,
    Bodyweight,
    Band,
}

/// Primary muscle groups for categorization and analytics.
#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, Eq, Copy)]
pub enum MuscleGroup {
    Chest,
    Back,
    Shoulders,
    Quads,
    Hamstrings,
    Glutes,
    Calves,
    Biceps,
    Triceps,
    Core,
}

/// Static definition of an exercise.
#[derive(Serialize, Deserialize, Clone, Debug, PartialEq)]
pub struct Exercise {
    pub id: Uuid,
    pub name: String,
    pub primary_muscle: MuscleGroup,
    pub equipment: EquipmentType,
    /// IDs of exercises that can serve as replacements for this one.
    pub alternative_exercise_ids: Vec<Uuid>,
    /// Flag indicating if this is a user-created exercise.
    pub is_custom: bool,
}

/// Individual log entry for a single set.
#[derive(Serialize, Deserialize, Clone, Debug, PartialEq)]
pub struct SetLog {
    pub id: Uuid,
    /// Granular ordering index for reordering within an exercise.
    pub sequence_order: u32,
    pub weight: f64,
    pub reps: u32,
    pub rpe: Option<f64>,
    pub set_type: SetType,
    pub is_completed: bool,
}

/// An exercise instance performed during a session.
#[derive(Serialize, Deserialize, Clone, Debug, PartialEq)]
pub struct ActiveExercise {
    pub exercise_id: Uuid,
    /// Order index for this exercise relative to others within its block or session.
    pub sequence_order: u32,
    pub sets: Vec<SetLog>,
    pub notes: Option<String>,
    /// Desired rest interval in seconds after each set.
    pub target_rest_time: Option<u32>,
}

/// High-level structural unit of a workout session.
///
/// Handled as a flat relational structure in the database and
/// serialized to JSON for the UniFFI bridge.
#[derive(Serialize, Deserialize, Clone, Debug, PartialEq)]
#[serde(tag = "type", content = "data")]
pub enum ExerciseBlock {
    Standard(ActiveExercise),
    Superset(Vec<ActiveExercise>),
    Circuit(Vec<ActiveExercise>),
}

/// The root entity representing a complete workout event.
#[derive(Serialize, Deserialize, Clone, Debug, PartialEq)]
pub struct WorkoutSession {
    pub id: Uuid,
    pub template_id: Option<Uuid>,
    pub name: String,
    pub start_time: i64,
    pub end_time: Option<i64>,
    pub exercise_blocks: Vec<ExerciseBlock>,
    pub overall_notes: Option<String>,
}

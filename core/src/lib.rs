//! UniFFI Bridge Layer for the Workout Tracker.
//!
//! This file implements the `WorkoutEngine`, which is the primary entry point
//! for the Android application to interact with the Rust core.

use std::sync::{Arc, Mutex};
use rusqlite::Connection;
use uuid::Uuid;
use serde_json;

pub mod domain;
pub mod db;

use crate::domain::*;
use crate::db::*;

// UniFFI setup
uniffi::setup_scaffolding!();

#[derive(Debug, thiserror::Error, uniffi::Error)]
pub enum WorkoutError {
    #[error("Database error: {0}")]
    DatabaseError(String),
    #[error("Serialization error: {0}")]
    SerializationError(String),
    #[error("Not found: {0}")]
    NotFound(String),
    #[error("Invalid argument: {0}")]
    InvalidArgument(String),
}

impl From<rusqlite::Error> for WorkoutError {
    fn from(e: rusqlite::Error) -> Self {
        WorkoutError::DatabaseError(e.to_string())
    }
}

impl From<serde_json::Error> for WorkoutError {
    fn from(e: serde_json::Error) -> Self {
        WorkoutError::SerializationError(e.to_string())
    }
}

/// The central manager for the workout tracking logic, exposed to Kotlin.
#[derive(uniffi::Object)]
pub struct WorkoutEngine {
    db_conn: Arc<Mutex<Connection>>,
}

#[uniffi::export]
impl WorkoutEngine {
    /// Initializes the engine with a database at the given path.
    #[uniffi::constructor]
    pub fn new(db_path: String) -> Result<Arc<Self>, WorkoutError> {
        let conn = if db_path == ":memory:" {
            Connection::open_in_memory()?
        } else {
            Connection::open(db_path)?
        };

        initialize_db(&conn)?;

        Ok(Arc::new(Self {
            db_conn: Arc::new(Mutex::new(conn)),
        }))
    }

    /// Creates a new exercise in the library.
    pub fn create_exercise(
        &self,
        name: String,
        muscle_group: String,
        equipment: String,
    ) -> Result<String, WorkoutError> {
        let conn = self.db_conn.lock().unwrap();
        let repo = ExerciseRepository::new(&conn);

        let muscle: MuscleGroup = serde_json::from_str(&format!("\"{}\"", muscle_group))
            .map_err(|_| WorkoutError::InvalidArgument(format!("Invalid muscle group: {}", muscle_group)))?;
        let equip: EquipmentType = serde_json::from_str(&format!("\"{}\"", equipment))
            .map_err(|_| WorkoutError::InvalidArgument(format!("Invalid equipment type: {}", equipment)))?;

        let exercise = Exercise {
            id: Uuid::new_v4(),
            name,
            primary_muscle: muscle,
            equipment: equip,
            alternative_exercise_ids: Vec::new(),
            is_custom: true,
        };

        repo.insert(&exercise)?;
        Ok(exercise.id.to_string())
    }

    /// Returns all exercises as a JSON serialized string.
    pub fn get_all_exercises(&self) -> Result<String, WorkoutError> {
        let conn = self.db_conn.lock().unwrap();
        let repo = ExerciseRepository::new(&conn);
        let exercises = repo.get_all()?;
        Ok(serde_json::to_string(&exercises)?)
    }

    /// Starts a new workout session.
    pub fn start_workout_session(
        &self,
        name: String,
        template_id: Option<String>,
    ) -> Result<String, WorkoutError> {
        let conn = self.db_conn.lock().unwrap();
        let repo = SessionRepository::new(&conn);

        let session = WorkoutSession {
            id: Uuid::new_v4(),
            template_id: template_id.map(|s| Uuid::parse_str(&s)).transpose()
                .map_err(|_| WorkoutError::InvalidArgument("Invalid template UUID".into()))?,
            name,
            start_time: chrono::Utc::now().timestamp(),
            end_time: None,
            exercise_blocks: Vec::new(),
            overall_notes: None,
        };

        repo.insert(&session)?;
        Ok(serde_json::to_string(&session)?)
    }

    /// Retrieves a specific workout session by ID.
    pub fn get_workout_session(&self, session_id: String) -> Result<String, WorkoutError> {
        let conn = self.db_conn.lock().unwrap();
        let repo = SessionRepository::new(&conn);
        let id = Uuid::parse_str(&session_id)
            .map_err(|_| WorkoutError::InvalidArgument("Invalid session UUID".into()))?;

        match repo.get_by_id(id)? {
            Some(s) => Ok(serde_json::to_string(&s)?),
            None => Err(WorkoutError::NotFound(format!("Session {} not found", session_id))),
        }
    }

    /// Adds an exercise block (Standard for now) to an existing session.
    pub fn add_exercise_block_to_session(
        &self,
        _session_id: String,
        exercise_id: String,
        block_type: String,
    ) -> Result<String, WorkoutError> {
        let e_id = Uuid::parse_str(&exercise_id)
            .map_err(|_| WorkoutError::InvalidArgument("Invalid exercise UUID".into()))?;

        let active_ex = ActiveExercise {
            exercise_id: e_id,
            sequence_order: 0,
            sets: Vec::new(),
            notes: None,
            target_rest_time: None,
        };

        let block = match block_type.as_str() {
            "Standard" => ExerciseBlock::Standard(active_ex),
            _ => return Err(WorkoutError::InvalidArgument("Only 'Standard' supported via bridge currently".into())),
        };

        Ok(serde_json::to_string(&block)?)
    }

    /// Logs a set for an active exercise.
    pub fn log_set(
        &self,
        _active_exercise_id: String,
        weight: f64,
        reps: u32,
        rpe: Option<f64>,
        set_type: String,
    ) -> Result<String, WorkoutError> {
        let st: SetType = serde_json::from_str(&format!("\"{}\"", set_type))
            .map_err(|_| WorkoutError::InvalidArgument(format!("Invalid set type: {}", set_type)))?;

        let set = SetLog {
            id: Uuid::new_v4(),
            sequence_order: 0,
            weight,
            reps,
            rpe,
            set_type: st,
            is_completed: true,
        };

        Ok(serde_json::to_string(&set)?)
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_engine_init() {
        let engine = WorkoutEngine::new(":memory:".to_string()).unwrap();
        let exercises_json = engine.get_all_exercises().unwrap();
        assert_eq!(exercises_json, "[]");
    }

    #[test]
    fn test_create_exercise_flow() {
        let engine = WorkoutEngine::new(":memory:".to_string()).unwrap();
        let id = engine.create_exercise("Bench Press".into(), "Chest".into(), "Barbell".into()).unwrap();
        assert!(!id.is_empty());

        let exercises = engine.get_all_exercises().unwrap();
        assert!(exercises.contains("Bench Press"));
    }
}

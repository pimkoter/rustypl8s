//! Database layer for the Workout Tracker using `rusqlite`.

use rusqlite::{params, Connection, Result};
use uuid::Uuid;
use crate::domain::*;
use std::collections::HashMap;

/// Initializes the database schema.
pub fn initialize_db(conn: &Connection) -> Result<()> {
    conn.execute("PRAGMA foreign_keys = ON;", [])?;
    conn.execute_batch(
        r#"
        CREATE TABLE IF NOT EXISTS exercises (
            id TEXT PRIMARY KEY,
            name TEXT NOT NULL,
            primary_muscle TEXT NOT NULL,
            equipment TEXT NOT NULL,
            is_custom BOOLEAN NOT NULL
        );

        CREATE TABLE IF NOT EXISTS exercise_alternatives (
            exercise_id TEXT NOT NULL,
            alternative_id TEXT NOT NULL,
            PRIMARY KEY (exercise_id, alternative_id),
            FOREIGN KEY(exercise_id) REFERENCES exercises(id),
            FOREIGN KEY(alternative_id) REFERENCES exercises(id)
        );

        CREATE TABLE IF NOT EXISTS workout_sessions (
            id TEXT PRIMARY KEY,
            template_id TEXT,
            name TEXT NOT NULL,
            start_time INTEGER NOT NULL,
            end_time INTEGER,
            overall_notes TEXT
        );

        CREATE TABLE IF NOT EXISTS active_exercises (
            id TEXT PRIMARY KEY,
            session_id TEXT NOT NULL,
            exercise_id TEXT NOT NULL,
            block_id TEXT NOT NULL,
            block_type TEXT NOT NULL,
            sequence_order INTEGER NOT NULL,
            notes TEXT,
            target_rest_time INTEGER,
            FOREIGN KEY(session_id) REFERENCES workout_sessions(id),
            FOREIGN KEY(exercise_id) REFERENCES exercises(id)
        );

        CREATE TABLE IF NOT EXISTS set_logs (
            id TEXT PRIMARY KEY,
            active_exercise_id TEXT NOT NULL,
            sequence_order INTEGER NOT NULL,
            weight REAL NOT NULL,
            reps INTEGER NOT NULL,
            rpe REAL,
            set_type TEXT NOT NULL,
            is_completed BOOLEAN NOT NULL,
            FOREIGN KEY(active_exercise_id) REFERENCES active_exercises(id)
        );

        CREATE INDEX IF NOT EXISTS idx_set_logs_active_exercise ON set_logs(active_exercise_id);
        CREATE INDEX IF NOT EXISTS idx_active_exercises_session ON active_exercises(session_id);
        "#,
    )
}

/// Repository for Exercises
pub struct ExerciseRepository<'a> {
    conn: &'a Connection,
}

impl<'a> ExerciseRepository<'a> {
    pub fn new(conn: &'a Connection) -> Self {
        Self { conn }
    }

    pub fn insert(&self, exercise: &Exercise) -> Result<()> {
        self.conn.execute(
            "INSERT INTO exercises (id, name, primary_muscle, equipment, is_custom) VALUES (?1, ?2, ?3, ?4, ?5)",
            params![
                exercise.id.to_string(),
                exercise.name,
                serde_json::to_string(&exercise.primary_muscle).unwrap().replace('"', ""),
                serde_json::to_string(&exercise.equipment).unwrap().replace('"', ""),
                exercise.is_custom
            ],
        )?;

        for alt_id in &exercise.alternative_exercise_ids {
            let exists: bool = self.conn.query_row(
                "SELECT EXISTS(SELECT 1 FROM exercises WHERE id = ?1)",
                params![alt_id.to_string()],
                |row| row.get(0),
            )?;

            if exists {
                self.conn.execute(
                    "INSERT INTO exercise_alternatives (exercise_id, alternative_id) VALUES (?1, ?2)",
                    params![exercise.id.to_string(), alt_id.to_string()],
                )?;
            }
        }
        Ok(())
    }

    pub fn get_all(&self) -> Result<Vec<Exercise>> {
        let mut stmt = self.conn.prepare("SELECT id, name, primary_muscle, equipment, is_custom FROM exercises")?;
        let exercise_iter = stmt.query_map([], |row| {
            let id_str: String = row.get(0)?;
            let primary_muscle_str: String = row.get(2)?;
            let equipment_str: String = row.get(3)?;

            Ok(Exercise {
                id: Uuid::parse_str(&id_str).unwrap(),
                name: row.get(1)?,
                primary_muscle: serde_json::from_str(&format!("\"{}\"", primary_muscle_str)).unwrap(),
                equipment: serde_json::from_str(&format!("\"{}\"", equipment_str)).unwrap(),
                alternative_exercise_ids: Vec::new(),
                is_custom: row.get(4)?,
            })
        })?;

        let mut exercises = Vec::new();
        for exercise in exercise_iter {
            let mut ex = exercise?;
            let mut alt_stmt = self.conn.prepare("SELECT alternative_id FROM exercise_alternatives WHERE exercise_id = ?1")?;
            let alt_iter = alt_stmt.query_map(params![ex.id.to_string()], |row| {
                let alt_id_str: String = row.get(0)?;
                Ok(Uuid::parse_str(&alt_id_str).unwrap())
            })?;
            for alt_id in alt_iter {
                ex.alternative_exercise_ids.push(alt_id?);
            }
            exercises.push(ex);
        }
        Ok(exercises)
    }
}

/// Repository for Workout Sessions
pub struct SessionRepository<'a> {
    conn: &'a Connection,
}

impl<'a> SessionRepository<'a> {
    pub fn new(conn: &'a Connection) -> Self {
        Self { conn }
    }

    pub fn insert(&self, session: &WorkoutSession) -> Result<()> {
        self.conn.execute(
            "INSERT INTO workout_sessions (id, template_id, name, start_time, end_time, overall_notes) VALUES (?1, ?2, ?3, ?4, ?5, ?6)",
            params![
                session.id.to_string(),
                session.template_id.map(|u| u.to_string()),
                session.name,
                session.start_time,
                session.end_time,
                session.overall_notes
            ],
        )?;

        for block in &session.exercise_blocks {
            let block_id = Uuid::new_v4().to_string();
            let (block_type, exercises) = match block {
                ExerciseBlock::Standard(ex) => ("Standard", vec![ex]),
                ExerciseBlock::Superset(exs) => ("Superset", exs.iter().collect()),
                ExerciseBlock::Circuit(exs) => ("Circuit", exs.iter().collect()),
            };

            for (i, ex) in exercises.iter().enumerate() {
                let active_ex_id = Uuid::new_v4().to_string();
                self.conn.execute(
                    "INSERT INTO active_exercises (id, session_id, exercise_id, block_id, block_type, sequence_order, notes, target_rest_time) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8)",
                    params![
                        active_ex_id,
                        session.id.to_string(),
                        ex.exercise_id.to_string(),
                        block_id,
                        block_type,
                        i as u32,
                        ex.notes,
                        ex.target_rest_time
                    ],
                )?;

                for set in &ex.sets {
                    self.conn.execute(
                        "INSERT INTO set_logs (id, active_exercise_id, sequence_order, weight, reps, rpe, set_type, is_completed) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8)",
                        params![
                            set.id.to_string(),
                            active_ex_id,
                            set.sequence_order,
                            set.weight,
                            set.reps,
                            set.rpe,
                            serde_json::to_string(&set.set_type).unwrap().replace('"', ""),
                            set.is_completed
                        ],
                    )?;
                }
            }
        }
        Ok(())
    }

    pub fn get_by_id(&self, id: Uuid) -> Result<Option<WorkoutSession>> {
        let mut stmt = self.conn.prepare("SELECT id, template_id, name, start_time, end_time, overall_notes FROM workout_sessions WHERE id = ?1")?;
        let session_opt = stmt.query_row(params![id.to_string()], |row| {
            let tid_str: Option<String> = row.get(1)?;
            Ok(WorkoutSession {
                id,
                template_id: tid_str.map(|s| Uuid::parse_str(&s).unwrap()),
                name: row.get(2)?,
                start_time: row.get(3)?,
                end_time: row.get(4)?,
                exercise_blocks: Vec::new(),
                overall_notes: row.get(5)?,
            })
        });

        let mut session = match session_opt {
            Ok(s) => s,
            Err(rusqlite::Error::QueryReturnedNoRows) => return Ok(None),
            Err(e) => return Err(e),
        };

        // Get all active exercises for this session
        let mut stmt = self.conn.prepare("SELECT id, exercise_id, block_id, block_type, sequence_order, notes, target_rest_time FROM active_exercises WHERE session_id = ?1 ORDER BY block_id, sequence_order")?;
        let active_ex_iter = stmt.query_map(params![id.to_string()], |row| {
            let id_str: String = row.get(0)?;
            let ex_id_str: String = row.get(1)?;
            Ok((
                id_str,
                ActiveExercise {
                    exercise_id: Uuid::parse_str(&ex_id_str).unwrap(),
                    sequence_order: row.get(4)?,
                    sets: Vec::new(),
                    notes: row.get(5)?,
                    target_rest_time: row.get(6)?,
                },
                row.get::<_, String>(2)?, // block_id
                row.get::<_, String>(3)?, // block_type
            ))
        })?;

        let mut blocks_map: HashMap<String, (String, Vec<ActiveExercise>)> = HashMap::new();
        let mut block_order: Vec<String> = Vec::new();

        for entry in active_ex_iter {
            let (ae_id, mut active_ex, block_id, block_type) = entry?;

            // Get sets for this active exercise
            let mut set_stmt = self.conn.prepare("SELECT id, sequence_order, weight, reps, rpe, set_type, is_completed FROM set_logs WHERE active_exercise_id = ?1 ORDER BY sequence_order")?;
            let set_iter = set_stmt.query_map(params![ae_id], |row| {
                let sid_str: String = row.get(0)?;
                let stype_str: String = row.get(5)?;
                Ok(SetLog {
                    id: Uuid::parse_str(&sid_str).unwrap(),
                    sequence_order: row.get(1)?,
                    weight: row.get(2)?,
                    reps: row.get(3)?,
                    rpe: row.get(4)?,
                    set_type: serde_json::from_str(&format!("\"{}\"", stype_str)).unwrap(),
                    is_completed: row.get(6)?,
                })
            })?;
            for set in set_iter {
                active_ex.sets.push(set?);
            }

            if !blocks_map.contains_key(&block_id) {
                block_order.push(block_id.clone());
                blocks_map.insert(block_id.clone(), (block_type, Vec::new()));
            }
            blocks_map.get_mut(&block_id).unwrap().1.push(active_ex);
        }

        for bid in block_order {
            let (btype, exs) = blocks_map.remove(&bid).unwrap();
            let block = match btype.as_str() {
                "Standard" => ExerciseBlock::Standard(exs.into_iter().next().unwrap()),
                "Superset" => ExerciseBlock::Superset(exs),
                "Circuit" => ExerciseBlock::Circuit(exs),
                _ => panic!("Unknown block type"),
            };
            session.exercise_blocks.push(block);
        }

        Ok(Some(session))
    }
}

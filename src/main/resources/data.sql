CREATE TABLE IF NOT EXISTS app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(120) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_app_user_username UNIQUE (username),
    CONSTRAINT uk_app_user_email UNIQUE (email),
    CONSTRAINT ck_app_user_role CHECK (role IN ('ADMIN', 'USER'))
);

INSERT INTO app_user (username, email, password_hash, role, enabled, created_at)
VALUES
    -- plaintext password for both seeded users: password123
    ('admin', 'admin@gaintrack.local', '$2a$10$jTgiZH.KfbN4wSuITusPeOpiWtZZyByLzfWDhxtCPxxigbGUGL/DC', 'ADMIN', TRUE, CURRENT_TIMESTAMP),
    ('marko', 'user@gaintrack.local', '$2a$10$jTgiZH.KfbN4wSuITusPeOpiWtZZyByLzfWDhxtCPxxigbGUGL/DC', 'USER', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO UPDATE
SET email = EXCLUDED.email,
    password_hash = EXCLUDED.password_hash,
    role = EXCLUDED.role,
    enabled = EXCLUDED.enabled;

-- Muscle Groups
INSERT INTO muscle_group (name) VALUES
                                    ('Chest'),
                                    ('Back'),
                                    ('Shoulders'),
                                    ('Biceps'),
                                    ('Triceps'),
                                    ('Legs'),
                                    ('Core'),
                                    ('Glutes')
    ON CONFLICT (name) DO NOTHING;

-- Exercises
INSERT INTO exercise (name, description, type) VALUES
                                                   ('Bench Press', 'Barbell bench press on a flat bench.', 'STRENGTH'),
                                                   ('Deadlift', 'Conventional barbell deadlift.', 'STRENGTH'),
                                                   ('Squat', 'Barbell back squat.', 'STRENGTH'),
                                                   ('Overhead Press', 'Standing barbell overhead press.', 'STRENGTH'),
                                                   ('Pull Up', 'Bodyweight pull up on a bar.', 'STRENGTH'),
                                                   ('Barbell Row', 'Bent-over barbell row.', 'STRENGTH'),
                                                   ('Running', 'Outdoor or treadmill running.', 'CARDIO'),
                                                   ('Cycling', 'Stationary or outdoor cycling.', 'CARDIO'),
                                                   ('Plank', 'Isometric core hold.', 'STRENGTH'),
                                                   ('Yoga Flow', 'Full body flexibility and mobility routine.', 'FLEXIBILITY')
    ON CONFLICT (name) DO NOTHING;

-- Exercise - Muscle Group mappings
INSERT INTO exercise_muscle_group (exercise_id, muscle_group_id)
SELECT e.id, m.id
FROM (
         VALUES
             ('Bench Press', 'Chest'),
             ('Bench Press', 'Triceps'),
             ('Deadlift', 'Back'),
             ('Deadlift', 'Legs'),
             ('Deadlift', 'Glutes'),
             ('Squat', 'Legs'),
             ('Squat', 'Glutes'),
             ('Overhead Press', 'Shoulders'),
             ('Overhead Press', 'Triceps'),
             ('Pull Up', 'Back'),
             ('Pull Up', 'Biceps'),
             ('Barbell Row', 'Back'),
             ('Barbell Row', 'Biceps'),
             ('Plank', 'Core')
     ) AS mapping(exercise_name, muscle_group_name)
         JOIN exercise e ON e.name = mapping.exercise_name
         JOIN muscle_group m ON m.name = mapping.muscle_group_name
    ON CONFLICT (exercise_id, muscle_group_id) DO NOTHING;

-- Pre-defined workouts
INSERT INTO workout (name, description, owner_id, shared)
SELECT workout_name, workout_description, owner.id, shared
FROM (
         VALUES
             ('Push Day', 'Chest, shoulders and triceps workout.', 'admin', TRUE),
             ('Pull Day', 'Back and biceps workout.', 'admin', TRUE),
             ('Leg Day', 'Lower body workout focused on quads, glutes and hamstrings.', 'admin', TRUE),
             ('Upper Body', 'Upper body workout focused on chest, shoulders, back and arms.', 'admin', TRUE),
             ('Cardio & Mobility', 'Cardio and flexibility workout.', 'admin', TRUE),

             ('Full Body Starter', 'Simple full body workout for beginners.', 'marko', FALSE),
             ('Core Burner', 'Core-focused workout with strength and stability exercises.', 'marko', FALSE),
             ('Fat Burn Cardio', 'Higher intensity cardio workout for conditioning.', 'marko', FALSE),
             ('Mobility Flow', 'Light mobility and flexibility workout.', 'marko', FALSE)
     ) AS seed(workout_name, workout_description, owner_username, shared)
         JOIN app_user owner ON owner.username = seed.owner_username
    ON CONFLICT (name) DO NOTHING;

-- Workout - Exercise mappings
-- Workout - Exercise mappings
INSERT INTO workout_exercise (workout_id, exercise_id, position)
SELECT w.id, e.id, mapping.position
FROM (
         VALUES
             ('Push Day', 'Bench Press', 1),
             ('Push Day', 'Overhead Press', 2),

             ('Pull Day', 'Barbell Row', 1),
             ('Pull Day', 'Pull Up', 2),

             ('Leg Day', 'Squat', 1),
             ('Leg Day', 'Deadlift', 2),

             ('Upper Body', 'Bench Press', 1),
             ('Upper Body', 'Barbell Row', 2),
             ('Upper Body', 'Overhead Press', 3),
             ('Upper Body', 'Pull Up', 4),

             ('Cardio & Mobility', 'Running', 1),
             ('Cardio & Mobility', 'Yoga Flow', 2),

             ('Full Body Starter', 'Squat', 1),
             ('Full Body Starter', 'Bench Press', 2),
             ('Full Body Starter', 'Plank', 3),

             ('Core Burner', 'Plank', 1),

             ('Fat Burn Cardio', 'Running', 1),
             ('Fat Burn Cardio', 'Cycling', 2),

             ('Mobility Flow', 'Yoga Flow', 1)
     ) AS mapping(workout_name, exercise_name, position)
         JOIN workout w ON w.name = mapping.workout_name
         JOIN exercise e ON e.name = mapping.exercise_name
    ON CONFLICT (workout_id, exercise_id, position) DO NOTHING;

-- Workout exercise sets
INSERT INTO workout_exercise_set (workout_exercise_id, set_number, number_of_reps, weight, duration_minutes)
SELECT we.id, mapping.set_number, mapping.number_of_reps, mapping.weight, mapping.duration_minutes
FROM (
         VALUES
             ('Push Day', 'Bench Press', 1, 1, 8, 60, NULL),
             ('Push Day', 'Bench Press', 1, 2, 8, 60, NULL),
             ('Push Day', 'Bench Press', 1, 3, 6, 65, NULL),

             ('Push Day', 'Overhead Press', 2, 1, 10, 30, NULL),
             ('Push Day', 'Overhead Press', 2, 2, 10, 30, NULL),
             ('Push Day', 'Overhead Press', 2, 3, 8, 35, NULL),

             ('Pull Day', 'Barbell Row', 1, 1, 10, 50, NULL),
             ('Pull Day', 'Barbell Row', 1, 2, 10, 50, NULL),
             ('Pull Day', 'Barbell Row', 1, 3, 8, 55, NULL),

             ('Pull Day', 'Pull Up', 2, 1, 8, 0, NULL),
             ('Pull Day', 'Pull Up', 2, 2, 8, 0, NULL),
             ('Pull Day', 'Pull Up', 2, 3, 6, 0, NULL),

             ('Leg Day', 'Squat', 1, 1, 8, 80, NULL),
             ('Leg Day', 'Squat', 1, 2, 8, 80, NULL),
             ('Leg Day', 'Squat', 1, 3, 6, 90, NULL),

             ('Leg Day', 'Deadlift', 2, 1, 8, 100, NULL),
             ('Leg Day', 'Deadlift', 2, 2, 8, 100, NULL),
             ('Leg Day', 'Deadlift', 2, 3, 6, 110, NULL),

             ('Upper Body', 'Bench Press', 1, 1, 10, 50, NULL),
             ('Upper Body', 'Bench Press', 1, 2, 10, 50, NULL),
             ('Upper Body', 'Barbell Row', 2, 1, 10, 45, NULL),
             ('Upper Body', 'Barbell Row', 2, 2, 10, 45, NULL),
             ('Upper Body', 'Overhead Press', 3, 1, 10, 25, NULL),
             ('Upper Body', 'Overhead Press', 3, 2, 10, 25, NULL),
             ('Upper Body', 'Pull Up', 4, 1, 8, 0, NULL),
             ('Upper Body', 'Pull Up', 4, 2, 6, 0, NULL),

             ('Cardio & Mobility', 'Running', 1, 1, NULL, NULL, 20),
             ('Cardio & Mobility', 'Running', 1, 2, NULL, NULL, 15),
             ('Cardio & Mobility', 'Yoga Flow', 2, 1, NULL, NULL, 10),
             ('Cardio & Mobility', 'Yoga Flow', 2, 2, NULL, NULL, 12),

             ('Full Body Starter', 'Squat', 1, 1, 10, 40, NULL),
             ('Full Body Starter', 'Squat', 1, 2, 10, 40, NULL),
             ('Full Body Starter', 'Bench Press', 2, 1, 10, 30, NULL),
             ('Full Body Starter', 'Bench Press', 2, 2, 10, 30, NULL),
             ('Full Body Starter', 'Plank', 3, 1, 1, 0, NULL),
             ('Full Body Starter', 'Plank', 3, 2, 1, 0, NULL),

             ('Core Burner', 'Plank', 1, 1, 1, 0, NULL),
             ('Core Burner', 'Plank', 1, 2, 1, 0, NULL),
             ('Core Burner', 'Plank', 1, 3, 1, 0, NULL),

             ('Fat Burn Cardio', 'Running', 1, 1, NULL, NULL, 15),
             ('Fat Burn Cardio', 'Running', 1, 2, NULL, NULL, 10),
             ('Fat Burn Cardio', 'Cycling', 2, 1, NULL, NULL, 20),
             ('Fat Burn Cardio', 'Cycling', 2, 2, NULL, NULL, 15),

             ('Mobility Flow', 'Yoga Flow', 1, 1, NULL, NULL, 12),
             ('Mobility Flow', 'Yoga Flow', 1, 2, NULL, NULL, 15)
     ) AS mapping(workout_name, exercise_name, position, set_number, number_of_reps, weight, duration_minutes)
         JOIN workout w ON w.name = mapping.workout_name
         JOIN exercise e ON e.name = mapping.exercise_name
         JOIN workout_exercise we
              ON we.workout_id = w.id
                  AND we.exercise_id = e.id
                  AND we.position = mapping.position
    ON CONFLICT (workout_exercise_id, set_number) DO NOTHING;
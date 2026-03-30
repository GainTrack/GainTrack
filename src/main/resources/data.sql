-- Muscle Groups
INSERT INTO muscle_group (id, name) VALUES
    (1, 'Chest'),
    (2, 'Back'),
    (3, 'Shoulders'),
    (4, 'Biceps'),
    (5, 'Triceps'),
    (6, 'Legs'),
    (7, 'Core'),
    (8, 'Glutes')
ON CONFLICT (id) DO NOTHING;

-- Exercises
INSERT INTO exercise (id, name, description, type) VALUES
    (1, 'Bench Press', 'Barbell bench press on a flat bench.', 'STRENGTH'),
    (2, 'Deadlift', 'Conventional barbell deadlift.', 'STRENGTH'),
    (3, 'Squat', 'Barbell back squat.', 'STRENGTH'),
    (4, 'Overhead Press', 'Standing barbell overhead press.', 'STRENGTH'),
    (5, 'Pull Up', 'Bodyweight pull up on a bar.', 'STRENGTH'),
    (6, 'Barbell Row', 'Bent-over barbell row.', 'STRENGTH'),
    (7, 'Running', 'Outdoor or treadmill running.', 'CARDIO'),
    (8, 'Cycling', 'Stationary or outdoor cycling.', 'CARDIO'),
    (9, 'Plank', 'Isometric core hold.', 'STRENGTH'),
    (10, 'Yoga Flow', 'Full body flexibility and mobility routine.', 'FLEXIBILITY')
ON CONFLICT (id) DO NOTHING;

-- Exercise - Muscle Group mappings
INSERT INTO exercise_muscle (id, exercise_id, muscle_group_id) VALUES
    (1, 1, 1),   -- Bench Press -> Chest
    (2, 1, 5),   -- Bench Press -> Triceps
    (3, 2, 2),   -- Deadlift -> Back
    (4, 2, 6),   -- Deadlift -> Legs
    (5, 2, 8),   -- Deadlift -> Glutes
    (6, 3, 6),   -- Squat -> Legs
    (7, 3, 8),   -- Squat -> Glutes
    (8, 4, 3),   -- Overhead Press -> Shoulders
    (9, 4, 5),   -- Overhead Press -> Triceps
    (10, 5, 2),  -- Pull Up -> Back
    (11, 5, 4),  -- Pull Up -> Biceps
    (12, 6, 2),  -- Barbell Row -> Back
    (13, 6, 4),  -- Barbell Row -> Biceps
    (14, 9, 7)   -- Plank -> Core
ON CONFLICT (id) DO NOTHING;

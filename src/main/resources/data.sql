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

-- Workouts
INSERT INTO workout (name, description)
SELECT 'Upper Body Power', 'Chest, shoulders, and triceps focused strength session.'
WHERE NOT EXISTS (
    SELECT 1
    FROM workout
    WHERE name = 'Upper Body Power'
);

INSERT INTO workout (name, description)
SELECT 'Full Body Strength', 'Balanced full body lifting session with compound movements.'
WHERE NOT EXISTS (
    SELECT 1
    FROM workout
    WHERE name = 'Full Body Strength'
);

-- Workout exercises
INSERT INTO workout_exercise (workout_id, exercise_id, position)
SELECT w.id, e.id, mapping.position
FROM (
         VALUES
             ('Upper Body Power', 'Bench Press', 1),
             ('Upper Body Power', 'Overhead Press', 2),
             ('Upper Body Power', 'Pull Up', 3),
             ('Full Body Strength', 'Squat', 1),
             ('Full Body Strength', 'Deadlift', 2),
             ('Full Body Strength', 'Barbell Row', 3),
             ('Full Body Strength', 'Plank', 4)
     ) AS mapping(workout_name, exercise_name, position)
         JOIN workout w ON w.name = mapping.workout_name
         JOIN exercise e ON e.name = mapping.exercise_name
    ON CONFLICT (workout_id, position) DO NOTHING;

-- Workout exercise sets
INSERT INTO workout_exercise_set (workout_exercise_id, set_order, number_of_reps, weight_kg)
SELECT we.id, mapping.set_order, mapping.number_of_reps, mapping.weight_kg
FROM (
         VALUES
             ('Upper Body Power', 1, 1, 10, 60.00),
             ('Upper Body Power', 1, 2, 10, 60.00),
             ('Upper Body Power', 1, 3, 8, 65.00),
             ('Upper Body Power', 2, 1, 10, 35.00),
             ('Upper Body Power', 2, 2, 10, 35.00),
             ('Upper Body Power', 2, 3, 8, 40.00),
             ('Upper Body Power', 3, 1, 8, 0.00),
             ('Upper Body Power', 3, 2, 8, 0.00),
             ('Upper Body Power', 3, 3, 6, 0.00),
             ('Full Body Strength', 1, 1, 8, 80.00),
             ('Full Body Strength', 1, 2, 8, 80.00),
             ('Full Body Strength', 1, 3, 6, 90.00),
             ('Full Body Strength', 2, 1, 5, 110.00),
             ('Full Body Strength', 2, 2, 5, 110.00),
             ('Full Body Strength', 2, 3, 5, 115.00),
             ('Full Body Strength', 3, 1, 10, 55.00),
             ('Full Body Strength', 3, 2, 10, 55.00),
             ('Full Body Strength', 3, 3, 8, 60.00),
             ('Full Body Strength', 4, 1, 60, 0.00),
             ('Full Body Strength', 4, 2, 60, 0.00),
             ('Full Body Strength', 4, 3, 45, 0.00)
     ) AS mapping(workout_name, exercise_position, set_order, number_of_reps, weight_kg)
         JOIN workout w ON w.name = mapping.workout_name
         JOIN workout_exercise we
              ON we.workout_id = w.id
                  AND we.position = mapping.exercise_position
    ON CONFLICT (workout_exercise_id, set_order) DO NOTHING;

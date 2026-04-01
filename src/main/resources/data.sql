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
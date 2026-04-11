package hr.tvz.gaintrack.service;

import hr.tvz.gaintrack.model.Exercise;
import hr.tvz.gaintrack.dto.WorkoutCreate;
import hr.tvz.gaintrack.model.Workout;
import hr.tvz.gaintrack.model.WorkoutExercise;
import hr.tvz.gaintrack.model.WorkoutExerciseSet;
import hr.tvz.gaintrack.repository.ExerciseRepository;
import hr.tvz.gaintrack.repository.WorkoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class WorkoutService {
    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;

    public WorkoutService(WorkoutRepository workoutRepository, ExerciseRepository exerciseRepository) {
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
    }

    public List<Workout> getAllWorkouts() {
        return workoutRepository.findAllByOrderByNameAsc();
    }

    public Workout getWorkoutById(Long id) {
        return workoutRepository.findWithDetailsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workout not found with id: " + id));
    }

    public List<Exercise> findAllExercises() {
        return exerciseRepository.findAllByOrderByNameAsc();
    }

    @Transactional
    public Workout createWorkout(WorkoutCreate workoutCreate) {
        String workoutName = workoutCreate.getName() == null ? null : workoutCreate.getName().trim();

        if (workoutName == null || workoutName.isEmpty()) {
            throw new IllegalArgumentException("Workout name is required.");
        }

        if (workoutRepository.existsByNameIgnoreCase(workoutName)) {
            throw new IllegalArgumentException("A workout with this name already exists.");
        }

        Map<Long, Exercise> exercisesById = new LinkedHashMap<>();
        for (Exercise exercise : exerciseRepository.findAllById(
                workoutCreate.getExercises().stream()
                        .map(WorkoutCreate.WorkoutExerciseCreate::getExerciseId)
                        .toList())) {
            exercisesById.put(exercise.getId(), exercise);
        }

        Workout workout = new Workout();
        workout.setName(workoutName);
        workout.setDescription(workoutCreate.getDescription());
        workout.setWorkoutExercises(buildWorkoutExercises(workout, workoutCreate, exercisesById));

        return workoutRepository.save(workout);
    }

    private Set<WorkoutExercise> buildWorkoutExercises(Workout workout,
                                                      WorkoutCreate workoutCreate,
                                                      Map<Long, Exercise> exercisesById) {
        Set<WorkoutExercise> workoutExercises = new LinkedHashSet<>();

        for (int exerciseIndex = 0; exerciseIndex < workoutCreate.getExercises().size(); exerciseIndex++) {
            WorkoutCreate.WorkoutExerciseCreate workoutExerciseCreate = workoutCreate.getExercises().get(exerciseIndex);
            Exercise exercise = exercisesById.get(workoutExerciseCreate.getExerciseId());

            if (exercise == null) {
                throw new IllegalArgumentException("Selected exercise could not be found.");
            }

            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkout(workout);
            workoutExercise.setExercise(exercise);
            workoutExercise.setPosition(exerciseIndex + 1);
            workoutExercise.setSets(buildWorkoutExerciseSets(workoutExercise, workoutExerciseCreate));
            workoutExercises.add(workoutExercise);
        }

        return workoutExercises;
    }

    private Set<WorkoutExerciseSet> buildWorkoutExerciseSets(WorkoutExercise workoutExercise,
                                                             WorkoutCreate.WorkoutExerciseCreate workoutExerciseCreate) {
        Set<WorkoutExerciseSet> sets = new LinkedHashSet<>();

        for (WorkoutCreate.WorkoutExerciseSetCreate setCreate : workoutExerciseCreate.getSets()) {
            WorkoutExerciseSet workoutExerciseSet = new WorkoutExerciseSet();
            workoutExerciseSet.setWorkoutExercise(workoutExercise);
            workoutExerciseSet.setSetNumber(setCreate.getSetNumber());
            workoutExerciseSet.setNumberOfReps(setCreate.getNumberOfReps());
            workoutExerciseSet.setWeight(setCreate.getWeight());
            sets.add(workoutExerciseSet);
        }

        return sets;
    }
}

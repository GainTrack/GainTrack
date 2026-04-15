package hr.tvz.gaintrack.service;

import hr.tvz.gaintrack.dto.WorkoutCreate;
import hr.tvz.gaintrack.model.Exercise;
import hr.tvz.gaintrack.model.ExerciseType;
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
import java.util.stream.Collectors;

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
    public void deleteById(Long id) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workout not found with id: " + id));

        workoutRepository.delete(workout);
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

        List<WorkoutCreate.WorkoutExerciseCreate> activeExercises = workoutCreate.getExercises().stream()
                .filter(exercise -> !exercise.isItemForDelete())
                .collect(Collectors.toList());

        if (activeExercises.isEmpty()) {
            throw new IllegalArgumentException("Add at least one exercise.");
        }

        boolean hasExerciseWithoutSets = activeExercises.stream()
                .anyMatch(exercise -> exercise.getSets().stream().noneMatch(set -> !set.isItemForDelete()));

        if (hasExerciseWithoutSets) {
            throw new IllegalArgumentException("Each exercise must contain at least one set.");
        }

        Map<Long, Exercise> exercisesById = new LinkedHashMap<>();
        for (Exercise exercise : exerciseRepository.findAllById(
                activeExercises.stream()
                        .map(WorkoutCreate.WorkoutExerciseCreate::getExerciseId)
                        .filter(id -> id != null)
                        .toList())) {
            exercisesById.put(exercise.getId(), exercise);
        }

        Workout workout = new Workout();
        workout.setName(workoutName);
        workout.setDescription(workoutCreate.getDescription());
        workout.setWorkoutExercises(buildWorkoutExercises(workout, activeExercises, exercisesById));

        return workoutRepository.save(workout);
    }

    private Set<WorkoutExercise> buildWorkoutExercises(
            Workout workout,
            List<WorkoutCreate.WorkoutExerciseCreate> exercises,
            Map<Long, Exercise> exercisesById
    ) {
        Set<WorkoutExercise> workoutExercises = new LinkedHashSet<>();

        for (int exerciseIndex = 0; exerciseIndex < exercises.size(); exerciseIndex++) {
            WorkoutCreate.WorkoutExerciseCreate workoutExerciseCreate = exercises.get(exerciseIndex);
            Exercise exercise = exercisesById.get(workoutExerciseCreate.getExerciseId());

            if (exercise == null) {
                throw new IllegalArgumentException("Selected exercise could not be found.");
            }

            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkout(workout);
            workoutExercise.setExercise(exercise);
            workoutExercise.setPosition(exerciseIndex + 1);
            workoutExercise.setSets(buildWorkoutExerciseSets(workoutExercise, workoutExerciseCreate, exercise));

            workoutExercises.add(workoutExercise);
        }

        return workoutExercises;
    }

    private Set<WorkoutExerciseSet> buildWorkoutExerciseSets(
            WorkoutExercise workoutExercise,
            WorkoutCreate.WorkoutExerciseCreate workoutExerciseCreate,
            Exercise exercise
    ) {
        Set<WorkoutExerciseSet> sets = new LinkedHashSet<>();

        List<WorkoutCreate.WorkoutExerciseSetCreate> activeSets = workoutExerciseCreate.getSets().stream()
                .filter(set -> !set.isItemForDelete())
                .collect(Collectors.toList());

        for (int setIndex = 0; setIndex < activeSets.size(); setIndex++) {
            WorkoutCreate.WorkoutExerciseSetCreate setCreate = activeSets.get(setIndex);

            validateSetByExerciseType(setCreate, exercise);

            WorkoutExerciseSet workoutExerciseSet = new WorkoutExerciseSet();
            workoutExerciseSet.setWorkoutExercise(workoutExercise);
            workoutExerciseSet.setSetNumber(setIndex + 1);

            applySetValuesByExerciseType(workoutExerciseSet, setCreate, exercise);

            sets.add(workoutExerciseSet);
        }

        return sets;
    }

    private void validateSetByExerciseType(
            WorkoutCreate.WorkoutExerciseSetCreate setCreate,
            Exercise exercise
    ) {
        if (exercise.getType() == ExerciseType.STRENGTH) {
            if (setCreate.getNumberOfReps() == null) {
                throw new IllegalArgumentException("Reps are required for strength exercises.");
            }

            if (setCreate.getWeight() == null) {
                throw new IllegalArgumentException("Weight is required for strength exercises.");
            }
        } else if (exercise.getType() == ExerciseType.CARDIO
                || exercise.getType() == ExerciseType.FLEXIBILITY) {

            if (setCreate.getDurationMinutes() == null) {
                throw new IllegalArgumentException(
                        "Duration in minutes is required for cardio and flexibility exercises."
                );
            }
        }
    }

    private void applySetValuesByExerciseType(
            WorkoutExerciseSet workoutExerciseSet,
            WorkoutCreate.WorkoutExerciseSetCreate setCreate,
            Exercise exercise
    ) {
        if (exercise.getType() == ExerciseType.STRENGTH) {
            workoutExerciseSet.setNumberOfReps(setCreate.getNumberOfReps());
            workoutExerciseSet.setWeight(setCreate.getWeight());
            workoutExerciseSet.setDurationMinutes(null);
        } else if (exercise.getType() == ExerciseType.CARDIO
                || exercise.getType() == ExerciseType.FLEXIBILITY) {
            workoutExerciseSet.setNumberOfReps(null);
            workoutExerciseSet.setWeight(null);
            workoutExerciseSet.setDurationMinutes(setCreate.getDurationMinutes());
        }
    }
}
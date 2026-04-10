package hr.tvz.gaintrack.service;

import hr.tvz.gaintrack.model.Exercise;
import hr.tvz.gaintrack.model.Workout;
import hr.tvz.gaintrack.model.WorkoutExercise;
import hr.tvz.gaintrack.model.WorkoutExerciseSet;
import hr.tvz.gaintrack.model.form.WorkoutForm;
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
    public void deleteById(Long id) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workout not found with id: " + id));

        workoutRepository.delete(workout);
    }

    @Transactional
    public Workout createWorkout(WorkoutForm workoutForm) {
        String workoutName = workoutForm.getName() == null ? null : workoutForm.getName().trim();

        if (workoutName == null || workoutName.isEmpty()) {
            throw new IllegalArgumentException("Workout name is required.");
        }

        if (workoutRepository.existsByNameIgnoreCase(workoutName)) {
            throw new IllegalArgumentException("A workout with this name already exists.");
        }

        Map<Long, Exercise> exercisesById = new LinkedHashMap<>();
        for (Exercise exercise : exerciseRepository.findAllById(
                workoutForm.getExercises().stream()
                        .map(WorkoutForm.WorkoutExerciseForm::getExerciseId)
                        .toList())) {
            exercisesById.put(exercise.getId(), exercise);
        }

        Workout workout = new Workout();
        workout.setName(workoutName);
        workout.setDescription(workoutForm.getDescription());
        workout.setWorkoutExercises(buildWorkoutExercises(workout, workoutForm, exercisesById));

        return workoutRepository.save(workout);
    }

    private Set<WorkoutExercise> buildWorkoutExercises(Workout workout,
                                                      WorkoutForm workoutForm,
                                                      Map<Long, Exercise> exercisesById) {
        Set<WorkoutExercise> workoutExercises = new LinkedHashSet<>();

        for (int exerciseIndex = 0; exerciseIndex < workoutForm.getExercises().size(); exerciseIndex++) {
            WorkoutForm.WorkoutExerciseForm workoutExerciseForm = workoutForm.getExercises().get(exerciseIndex);
            Exercise exercise = exercisesById.get(workoutExerciseForm.getExerciseId());

            if (exercise == null) {
                throw new IllegalArgumentException("Selected exercise could not be found.");
            }

            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkout(workout);
            workoutExercise.setExercise(exercise);
            workoutExercise.setPosition(exerciseIndex + 1);
            workoutExercise.setSets(buildWorkoutExerciseSets(workoutExercise, workoutExerciseForm));
            workoutExercises.add(workoutExercise);
        }

        return workoutExercises;
    }

    private Set<WorkoutExerciseSet> buildWorkoutExerciseSets(WorkoutExercise workoutExercise,
                                                             WorkoutForm.WorkoutExerciseForm workoutExerciseForm) {
        Set<WorkoutExerciseSet> sets = new LinkedHashSet<>();

        for (WorkoutForm.WorkoutExerciseSetForm setForm : workoutExerciseForm.getSets()) {
            WorkoutExerciseSet workoutExerciseSet = new WorkoutExerciseSet();
            workoutExerciseSet.setWorkoutExercise(workoutExercise);
            workoutExerciseSet.setSetNumber(setForm.getSetNumber());
            workoutExerciseSet.setNumberOfReps(setForm.getNumberOfReps());
            workoutExerciseSet.setWeight(setForm.getWeight());
            sets.add(workoutExerciseSet);
        }

        return sets;
    }
}

package hr.tvz.gaintrack.service;

import hr.tvz.gaintrack.model.Exercise;
import hr.tvz.gaintrack.model.MuscleGroup;
import hr.tvz.gaintrack.repository.ExerciseRepository;
import hr.tvz.gaintrack.repository.MuscleGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final MuscleGroupRepository muscleGroupRepository;

    public ExerciseService(ExerciseRepository exerciseRepository, MuscleGroupRepository muscleGroupRepository) {
        this.exerciseRepository = exerciseRepository;
        this.muscleGroupRepository = muscleGroupRepository;
    }

    public List<Exercise> findAll() {
        return exerciseRepository.findAll();
    }

    public Exercise getExerciseById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found with id: " + id));
    }

    @Transactional
    public void deleteById(Long id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found with id: " + id));

        exercise.getMuscleGroups().clear();
        exerciseRepository.save(exercise);

        exerciseRepository.delete(exercise);
    }

    public List<Exercise> search(String search) {
        if (search == null || search.trim().isEmpty()) {
            return exerciseRepository.findAllByOrderByNameAsc();
        }

        String query = search.trim();
        return exerciseRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByNameAsc(query, query);
    }

    public List<MuscleGroup> findAllMuscleGroups() {
        return muscleGroupRepository.findAll();
    }

    @Transactional
    public void createExercise(Exercise exercise, List<Long> muscleGroupIds) {
        exercise.setMuscleGroups(resolveMuscleGroups(muscleGroupIds));

        exerciseRepository.save(exercise);
    }

    @Transactional
    public void updateExercise(Long id, Exercise exerciseData, List<Long> muscleGroupIds) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found with id: " + id));

        exercise.setName(exerciseData.getName());
        exercise.setDescription(exerciseData.getDescription());
        exercise.setType(exerciseData.getType());
        exercise.setMuscleGroups(resolveMuscleGroups(muscleGroupIds));

        exerciseRepository.save(exercise);
    }

    private LinkedHashSet<MuscleGroup> resolveMuscleGroups(List<Long> muscleGroupIds) {
        if (muscleGroupIds == null || muscleGroupIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        return new LinkedHashSet<>(muscleGroupRepository.findAllById(muscleGroupIds));
    }
}

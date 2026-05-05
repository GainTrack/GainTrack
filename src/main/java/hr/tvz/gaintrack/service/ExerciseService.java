package hr.tvz.gaintrack.service;

import hr.tvz.gaintrack.model.AppUser;
import hr.tvz.gaintrack.model.Exercise;
import hr.tvz.gaintrack.model.MuscleGroup;
import hr.tvz.gaintrack.repository.AppUserRepository;
import hr.tvz.gaintrack.repository.ExerciseRepository;
import hr.tvz.gaintrack.repository.MuscleGroupRepository;
import hr.tvz.gaintrack.repository.WorkoutExerciseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final MuscleGroupRepository muscleGroupRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final AppUserRepository appUserRepository;

    public ExerciseService(
            ExerciseRepository exerciseRepository,
            MuscleGroupRepository muscleGroupRepository,
            WorkoutExerciseRepository workoutExerciseRepository,
            AppUserRepository appUserRepository
    ) {
        this.exerciseRepository = exerciseRepository;
        this.muscleGroupRepository = muscleGroupRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.appUserRepository = appUserRepository;
    }

    public List<Exercise> findAll(String username) {
        return exerciseRepository.findVisibleByUsernameOrderByNameAsc(username);
    }

    public Exercise getExerciseById(Long id, String username) {
        return exerciseRepository.findVisibleByIdAndUsername(id, username)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found or not accessible."));
    }

    private Exercise getEditableExerciseById(Long id, String username, boolean admin) {
        if (admin) {
            return exerciseRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Exercise not found with id: " + id));
        }

        return exerciseRepository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found or not accessible."));
    }

    public Exercise getEditableExerciseForForm(Long id, String username, boolean admin) {
        return getEditableExerciseById(id, username, admin);
    }

    @Transactional
    public void deleteById(Long id, String username, boolean admin) {
        Exercise exercise = getEditableExerciseById(id, username, admin);

        if (workoutExerciseRepository.existsByExerciseId(id)) {
            throw new IllegalStateException("Exercise cannot be deleted because it is used in a workout.");
        }

        exercise.getMuscleGroups().clear();
        exerciseRepository.delete(exercise);
    }

    public List<Exercise> search(String search, String username) {
        if (search == null || search.trim().isEmpty()) {
            return exerciseRepository.findVisibleByUsernameOrderByNameAsc(username);
        }

        return exerciseRepository.searchVisibleByUsernameOrderByNameAsc(username, search.trim());
    }

    public List<MuscleGroup> findAllMuscleGroups() {
        return muscleGroupRepository.findAll();
    }

    @Transactional
    public void createExercise(Exercise exercise, List<Long> muscleGroupIds, String username) {
        AppUser owner = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        exercise.setOwner(owner);
        exercise.setShared(false);
        exercise.setMuscleGroups(resolveMuscleGroups(muscleGroupIds));
        exerciseRepository.save(exercise);
    }

    @Transactional
    public void updateExercise(Long id, Exercise exerciseData, List<Long> muscleGroupIds, String username, boolean admin) {
        Exercise exercise = getEditableExerciseById(id, username, admin);

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

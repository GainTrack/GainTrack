package hr.tvz.gaintrack.service;

import hr.tvz.gaintrack.dto.WorkoutCreate;
import hr.tvz.gaintrack.model.AppUser;
import hr.tvz.gaintrack.model.Exercise;
import hr.tvz.gaintrack.model.ExerciseType;
import hr.tvz.gaintrack.model.UserRole;
import hr.tvz.gaintrack.model.Workout;
import hr.tvz.gaintrack.model.WorkoutExercise;
import hr.tvz.gaintrack.model.WorkoutExerciseSet;
import hr.tvz.gaintrack.repository.AppUserRepository;
import hr.tvz.gaintrack.repository.ExerciseRepository;
import hr.tvz.gaintrack.repository.WorkoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private WorkoutService workoutService;

    private AppUser marko;
    private Exercise benchPress;
    private Exercise running;

    @BeforeEach
    void setUp() {
        marko = user(1L, "marko");
        benchPress = exercise(10L, "Bench Press", ExerciseType.STRENGTH);
        running = exercise(11L, "Running", ExerciseType.CARDIO);
    }

    @Test
    void getAllWorkouts_returnsVisibleWorkoutsForUser() {
        Workout workout = workout(1L, "Push Day", "Chest workout", marko, false);

        when(workoutRepository.findVisibleByUsernameOrderByNameAsc("marko"))
                .thenReturn(List.of(workout));

        List<Workout> result = workoutService.getAllWorkouts("marko");

        assertThat(result).isEqualTo(List.of(workout));

        verify(workoutRepository).findVisibleByUsernameOrderByNameAsc("marko");
    }

    @Test
    void getWorkoutById_returnsVisibleWorkout() {
        Workout workout = workout(1L, "Push Day", "Chest workout", marko, false);

        when(workoutRepository.findVisibleWithDetailsByIdAndUsername(1L, "marko"))
                .thenReturn(Optional.of(workout));

        Workout result = workoutService.getWorkoutById(1L, "marko");

        assertThat(result).isEqualTo(workout);

        verify(workoutRepository).findVisibleWithDetailsByIdAndUsername(1L, "marko");
    }

    @Test
    void getWorkoutById_throwsWhenWorkoutIsNotVisible() {
        when(workoutRepository.findVisibleWithDetailsByIdAndUsername(1L, "marko"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.getWorkoutById(1L, "marko"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Workout not found or not accessible.");
    }

    @Test
    void findAllExercises_returnsVisibleExercisesForUser() {
        when(exerciseRepository.findVisibleByUsernameOrderByNameAsc("marko"))
                .thenReturn(List.of(benchPress, running));

        List<Exercise> result = workoutService.findAllExercises("marko");

        assertThat(result).isEqualTo(List.of(benchPress, running));

        verify(exerciseRepository).findVisibleByUsernameOrderByNameAsc("marko");
    }

    @Test
    void createWorkout_savesStrengthWorkoutWithRepsAndWeight() {
        WorkoutCreate workoutCreate = workoutCreateWithStrengthExercise();

        when(workoutRepository.existsByNameIgnoreCase("Push Day")).thenReturn(false);
        when(exerciseRepository.findAllById(List.of(10L))).thenReturn(List.of(benchPress));
        when(appUserRepository.findByUsername("marko")).thenReturn(Optional.of(marko));
        when(workoutRepository.save(org.mockito.Mockito.any(Workout.class)))
                .thenAnswer(invocation -> {
                    Workout savedWorkout = invocation.getArgument(0);
                    savedWorkout.setId(1L);
                    return savedWorkout;
                });

        Workout result = workoutService.createWorkout(workoutCreate, "marko");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Push Day");
        assertThat(result.getDescription()).isEqualTo("Chest and triceps workout");
        assertThat(result.getOwner()).isEqualTo(marko);
        assertThat(result.isShared()).isFalse();

        ArgumentCaptor<Workout> workoutCaptor = ArgumentCaptor.forClass(Workout.class);
        verify(workoutRepository).save(workoutCaptor.capture());

        Workout savedWorkout = workoutCaptor.getValue();
        assertThat(savedWorkout.getWorkoutExercises()).hasSize(1);

        WorkoutExercise savedWorkoutExercise = savedWorkout.getWorkoutExercises().iterator().next();
        assertThat(savedWorkoutExercise.getWorkout()).isEqualTo(savedWorkout);
        assertThat(savedWorkoutExercise.getExercise()).isEqualTo(benchPress);
        assertThat(savedWorkoutExercise.getPosition()).isEqualTo(1);
        assertThat(savedWorkoutExercise.getSets()).hasSize(1);

        WorkoutExerciseSet savedSet = savedWorkoutExercise.getSets().iterator().next();
        assertThat(savedSet.getWorkoutExercise()).isEqualTo(savedWorkoutExercise);
        assertThat(savedSet.getSetNumber()).isEqualTo(1);
        assertThat(savedSet.getNumberOfReps()).isEqualTo(10);
        assertThat(savedSet.getWeight()).isEqualTo(60.0);
        assertThat(savedSet.getDurationMinutes()).isNull();
    }

    @Test
    void createWorkout_savesCardioWorkoutWithDurationOnly() {
        WorkoutCreate workoutCreate = workoutCreateWithCardioExercise();

        when(workoutRepository.existsByNameIgnoreCase("Cardio Day")).thenReturn(false);
        when(exerciseRepository.findAllById(List.of(11L))).thenReturn(List.of(running));
        when(appUserRepository.findByUsername("marko")).thenReturn(Optional.of(marko));
        when(workoutRepository.save(org.mockito.Mockito.any(Workout.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Workout result = workoutService.createWorkout(workoutCreate, "marko");

        WorkoutExercise savedWorkoutExercise = result.getWorkoutExercises().iterator().next();
        WorkoutExerciseSet savedSet = savedWorkoutExercise.getSets().iterator().next();

        assertThat(savedWorkoutExercise.getExercise()).isEqualTo(running);
        assertThat(savedSet.getSetNumber()).isEqualTo(1);
        assertThat(savedSet.getNumberOfReps()).isNull();
        assertThat(savedSet.getWeight()).isNull();
        assertThat(savedSet.getDurationMinutes()).isEqualTo(30);
    }

    @Test
    void createWorkout_trimsWorkoutNameBeforeSaving() {
        WorkoutCreate workoutCreate = workoutCreateWithStrengthExercise();
        workoutCreate.setName("  Push Day  ");

        when(workoutRepository.existsByNameIgnoreCase("Push Day")).thenReturn(false);
        when(exerciseRepository.findAllById(List.of(10L))).thenReturn(List.of(benchPress));
        when(appUserRepository.findByUsername("marko")).thenReturn(Optional.of(marko));
        when(workoutRepository.save(org.mockito.Mockito.any(Workout.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Workout result = workoutService.createWorkout(workoutCreate, "marko");

        assertThat(result.getName()).isEqualTo("Push Day");
    }

    @Test
    void createWorkout_throwsWhenWorkoutNameAlreadyExists() {
        WorkoutCreate workoutCreate = workoutCreateWithStrengthExercise();

        when(workoutRepository.existsByNameIgnoreCase("Push Day")).thenReturn(true);

        assertThatThrownBy(() -> workoutService.createWorkout(workoutCreate, "marko"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A workout with this name already exists.");
    }

    @Test
    void createWorkout_throwsWhenWorkoutNameIsBlank() {
        WorkoutCreate workoutCreate = workoutCreateWithStrengthExercise();
        workoutCreate.setName("   ");

        assertThatThrownBy(() -> workoutService.createWorkout(workoutCreate, "marko"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Workout name is required.");
    }

    @Test
    void createWorkout_throwsWhenNoActiveExercisesExist() {
        WorkoutCreate workoutCreate = new WorkoutCreate();
        workoutCreate.setName("Empty Workout");

        assertThatThrownBy(() -> workoutService.createWorkout(workoutCreate, "marko"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Add at least one exercise.");
    }

    @Test
    void createWorkout_throwsWhenExerciseHasNoActiveSets() {
        WorkoutCreate workoutCreate = new WorkoutCreate();
        workoutCreate.setName("Invalid Workout");

        WorkoutCreate.WorkoutExerciseCreate exerciseCreate = new WorkoutCreate.WorkoutExerciseCreate();
        exerciseCreate.setExerciseId(10L);

        WorkoutCreate.WorkoutExerciseSetCreate deletedSet = new WorkoutCreate.WorkoutExerciseSetCreate();
        deletedSet.setItemForDelete(true);

        exerciseCreate.getSets().add(deletedSet);
        workoutCreate.getExercises().add(exerciseCreate);

        assertThatThrownBy(() -> workoutService.createWorkout(workoutCreate, "marko"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Each exercise must contain at least one set.");
    }

    @Test
    void createWorkout_throwsWhenSelectedExerciseDoesNotExist() {
        WorkoutCreate workoutCreate = workoutCreateWithStrengthExercise();

        when(workoutRepository.existsByNameIgnoreCase("Push Day")).thenReturn(false);
        when(exerciseRepository.findAllById(List.of(10L))).thenReturn(List.of());
        when(appUserRepository.findByUsername("marko")).thenReturn(Optional.of(marko));

        assertThatThrownBy(() -> workoutService.createWorkout(workoutCreate, "marko"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Selected exercise could not be found.");
    }

    @Test
    void createWorkout_throwsWhenStrengthExerciseHasNoReps() {
        WorkoutCreate workoutCreate = workoutCreateWithStrengthExercise();
        workoutCreate.getExercises().get(0).getSets().get(0).setNumberOfReps(null);

        when(workoutRepository.existsByNameIgnoreCase("Push Day")).thenReturn(false);
        when(exerciseRepository.findAllById(List.of(10L))).thenReturn(List.of(benchPress));
        when(appUserRepository.findByUsername("marko")).thenReturn(Optional.of(marko));

        assertThatThrownBy(() -> workoutService.createWorkout(workoutCreate, "marko"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reps are required for strength exercises.");
    }

    @Test
    void createWorkout_throwsWhenStrengthExerciseHasNoWeight() {
        WorkoutCreate workoutCreate = workoutCreateWithStrengthExercise();
        workoutCreate.getExercises().get(0).getSets().get(0).setWeight(null);

        when(workoutRepository.existsByNameIgnoreCase("Push Day")).thenReturn(false);
        when(exerciseRepository.findAllById(List.of(10L))).thenReturn(List.of(benchPress));
        when(appUserRepository.findByUsername("marko")).thenReturn(Optional.of(marko));

        assertThatThrownBy(() -> workoutService.createWorkout(workoutCreate, "marko"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Weight is required for strength exercises.");
    }

    @Test
    void createWorkout_throwsWhenCardioExerciseHasNoDuration() {
        WorkoutCreate workoutCreate = workoutCreateWithCardioExercise();
        workoutCreate.getExercises().get(0).getSets().get(0).setDurationMinutes(null);

        when(workoutRepository.existsByNameIgnoreCase("Cardio Day")).thenReturn(false);
        when(exerciseRepository.findAllById(List.of(11L))).thenReturn(List.of(running));
        when(appUserRepository.findByUsername("marko")).thenReturn(Optional.of(marko));

        assertThatThrownBy(() -> workoutService.createWorkout(workoutCreate, "marko"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Duration in minutes is required for cardio and flexibility exercises.");
    }

    @Test
    void getWorkoutFormById_convertsWorkoutToWorkoutCreateSortedByPositionAndSetNumber() {
        Workout workout = workout(1L, "Push Day", "Chest workout", marko, false);

        WorkoutExercise secondExercise = workoutExercise(2L, workout, running, 2);
        secondExercise.setSets(new LinkedHashSet<>(List.of(
                workoutExerciseSet(3L, secondExercise, 2, null, null, 20),
                workoutExerciseSet(4L, secondExercise, 1, null, null, 10)
        )));

        WorkoutExercise firstExercise = workoutExercise(1L, workout, benchPress, 1);
        firstExercise.setSets(new LinkedHashSet<>(List.of(
                workoutExerciseSet(2L, firstExercise, 2, 8, 70.0, null),
                workoutExerciseSet(1L, firstExercise, 1, 10, 60.0, null)
        )));

        workout.setWorkoutExercises(new LinkedHashSet<>(List.of(secondExercise, firstExercise)));

        when(workoutRepository.findWithDetailsByIdAndOwnerUsername(1L, "marko"))
                .thenReturn(Optional.of(workout));

        WorkoutCreate result = workoutService.getWorkoutFormById(1L, "marko", false);

        assertThat(result.getName()).isEqualTo("Push Day");
        assertThat(result.getDescription()).isEqualTo("Chest workout");
        assertThat(result.getExercises()).hasSize(2);

        assertThat(result.getExercises().get(0).getExerciseId()).isEqualTo(10L);
        assertThat(result.getExercises().get(0).getSets()).hasSize(2);
        assertThat(result.getExercises().get(0).getSets().get(0).getSetNumber()).isEqualTo(1);
        assertThat(result.getExercises().get(0).getSets().get(0).getNumberOfReps()).isEqualTo(10);
        assertThat(result.getExercises().get(0).getSets().get(0).getWeight()).isEqualTo(60.0);

        assertThat(result.getExercises().get(1).getExerciseId()).isEqualTo(11L);
        assertThat(result.getExercises().get(1).getSets().get(0).getSetNumber()).isEqualTo(1);
        assertThat(result.getExercises().get(1).getSets().get(0).getDurationMinutes()).isEqualTo(10);
    }

    @Test
    void deleteById_deletesOwnedWorkoutForRegularUser() {
        Workout workout = workout(1L, "Push Day", "Chest workout", marko, false);

        when(workoutRepository.findWithDetailsByIdAndOwnerUsername(1L, "marko"))
                .thenReturn(Optional.of(workout));

        workoutService.deleteById(1L, "marko", false);

        verify(workoutRepository).delete(workout);
    }

    @Test
    void deleteById_deletesAnyWorkoutForAdmin() {
        Workout workout = workout(1L, "Push Day", "Chest workout", marko, false);

        when(workoutRepository.findWithDetailsById(1L))
                .thenReturn(Optional.of(workout));

        workoutService.deleteById(1L, "admin", true);

        verify(workoutRepository).delete(workout);
    }

    private static AppUser user(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("hash");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        return user;
    }

    private static Exercise exercise(Long id, String name, ExerciseType type) {
        Exercise exercise = new Exercise();
        exercise.setId(id);
        exercise.setName(name);
        exercise.setDescription(name + " description");
        exercise.setType(type);
        return exercise;
    }

    private static Workout workout(Long id, String name, String description, AppUser owner, boolean shared) {
        Workout workout = new Workout();
        workout.setId(id);
        workout.setName(name);
        workout.setDescription(description);
        workout.setOwner(owner);
        workout.setShared(shared);
        workout.setWorkoutExercises(new LinkedHashSet<>());
        return workout;
    }

    private static WorkoutExercise workoutExercise(Long id, Workout workout, Exercise exercise, Integer position) {
        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.setId(id);
        workoutExercise.setWorkout(workout);
        workoutExercise.setExercise(exercise);
        workoutExercise.setPosition(position);
        workoutExercise.setSets(new LinkedHashSet<>());
        return workoutExercise;
    }

    private static WorkoutExerciseSet workoutExerciseSet(Long id,
                                                         WorkoutExercise workoutExercise,
                                                         Integer setNumber,
                                                         Integer reps,
                                                         Double weight,
                                                         Integer durationMinutes) {
        WorkoutExerciseSet set = new WorkoutExerciseSet();
        set.setId(id);
        set.setWorkoutExercise(workoutExercise);
        set.setSetNumber(setNumber);
        set.setNumberOfReps(reps);
        set.setWeight(weight);
        set.setDurationMinutes(durationMinutes);
        return set;
    }

    private static WorkoutCreate workoutCreateWithStrengthExercise() {
        WorkoutCreate workoutCreate = new WorkoutCreate();
        workoutCreate.setName("Push Day");
        workoutCreate.setDescription("Chest and triceps workout");

        WorkoutCreate.WorkoutExerciseCreate exerciseCreate = new WorkoutCreate.WorkoutExerciseCreate();
        exerciseCreate.setExerciseId(10L);

        WorkoutCreate.WorkoutExerciseSetCreate setCreate = new WorkoutCreate.WorkoutExerciseSetCreate();
        setCreate.setSetNumber(1);
        setCreate.setNumberOfReps(10);
        setCreate.setWeight(60.0);

        exerciseCreate.getSets().add(setCreate);
        workoutCreate.getExercises().add(exerciseCreate);

        return workoutCreate;
    }

    private static WorkoutCreate workoutCreateWithCardioExercise() {
        WorkoutCreate workoutCreate = new WorkoutCreate();
        workoutCreate.setName("Cardio Day");
        workoutCreate.setDescription("Running workout");

        WorkoutCreate.WorkoutExerciseCreate exerciseCreate = new WorkoutCreate.WorkoutExerciseCreate();
        exerciseCreate.setExerciseId(11L);

        WorkoutCreate.WorkoutExerciseSetCreate setCreate = new WorkoutCreate.WorkoutExerciseSetCreate();
        setCreate.setSetNumber(1);
        setCreate.setDurationMinutes(30);

        exerciseCreate.getSets().add(setCreate);
        workoutCreate.getExercises().add(exerciseCreate);

        return workoutCreate;
    }
}
package hr.tvz.gaintrack.service;

import hr.tvz.gaintrack.model.AppUser;
import hr.tvz.gaintrack.model.Exercise;
import hr.tvz.gaintrack.model.ExerciseType;
import hr.tvz.gaintrack.model.MuscleGroup;
import hr.tvz.gaintrack.repository.AppUserRepository;
import hr.tvz.gaintrack.repository.ExerciseRepository;
import hr.tvz.gaintrack.repository.MuscleGroupRepository;
import hr.tvz.gaintrack.repository.WorkoutExerciseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private MuscleGroupRepository muscleGroupRepository;

    @Mock
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private ExerciseService exerciseService;

    @Captor
    private ArgumentCaptor<Exercise> exerciseCaptor;

    private AppUser owner;
    private MuscleGroup chest;
    private MuscleGroup back;
    private Exercise exercise;

    @BeforeEach
    void setUp() {
        owner = user(1L, "marko");
        chest = muscleGroup(10L, "Chest");
        back = muscleGroup(11L, "Back");
        exercise = exercise(100L, "Bench Press", "Chest exercise", ExerciseType.STRENGTH, owner, true, Set.of(chest));
    }

    @Test
    void findAll_returnsVisibleExercisesForUser() {
        when(exerciseRepository.findVisibleByUsernameOrderByNameAsc("marko")).thenReturn(List.of(exercise));

        List<Exercise> result = exerciseService.findAll("marko");

        assertThat(result).containsExactly(exercise);
        verify(exerciseRepository).findVisibleByUsernameOrderByNameAsc("marko");
    }

    @Test
    void getExerciseById_returnsVisibleExercise() {
        when(exerciseRepository.findVisibleByIdAndUsername(100L, "marko")).thenReturn(Optional.of(exercise));

        Exercise result = exerciseService.getExerciseById(100L, "marko");

        assertThat(result).isSameAs(exercise);
        verify(exerciseRepository).findVisibleByIdAndUsername(100L, "marko");
    }

    @Test
    void getExerciseById_throwsWhenExerciseNotVisible() {
        when(exerciseRepository.findVisibleByIdAndUsername(100L, "marko")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exerciseService.getExerciseById(100L, "marko"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exercise not found or not accessible.");
    }

    @Test
    void getEditableExerciseForForm_nonAdminUsesOwnerQuery() {
        when(exerciseRepository.findByIdAndOwnerUsername(100L, "marko")).thenReturn(Optional.of(exercise));

        Exercise result = exerciseService.getEditableExerciseForForm(100L, "marko", false);

        assertThat(result).isSameAs(exercise);
        verify(exerciseRepository).findByIdAndOwnerUsername(100L, "marko");
        verify(exerciseRepository, never()).findById(100L);
    }

    @Test
    void getEditableExerciseForForm_adminCanLoadAnyExercise() {
        when(exerciseRepository.findById(100L)).thenReturn(Optional.of(exercise));

        Exercise result = exerciseService.getEditableExerciseForForm(100L, "marko", true);

        assertThat(result).isSameAs(exercise);
        verify(exerciseRepository).findById(100L);
        verify(exerciseRepository, never()).findByIdAndOwnerUsername(100L, "marko");
    }

    @Test
    void getEditableExerciseForForm_throwsWhenExerciseNotEditable() {
        when(exerciseRepository.findByIdAndOwnerUsername(100L, "marko")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exerciseService.getEditableExerciseForForm(100L, "marko", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exercise not found or not accessible.");
    }

    @Test
    void search_blankSearchFallsBackToVisibleExercises() {
        when(exerciseRepository.findVisibleByUsernameOrderByNameAsc("marko")).thenReturn(List.of(exercise));

        List<Exercise> result = exerciseService.search("   ", "marko");

        assertThat(result).containsExactly(exercise);
        verify(exerciseRepository).findVisibleByUsernameOrderByNameAsc("marko");
        verify(exerciseRepository, never()).searchVisibleByUsernameOrderByNameAsc("marko", "");
    }

    @Test
    void search_nonBlankSearchIsTrimmedBeforeQuerying() {
        when(exerciseRepository.searchVisibleByUsernameOrderByNameAsc("marko", "press")).thenReturn(List.of(exercise));

        List<Exercise> result = exerciseService.search("  press  ", "marko");

        assertThat(result).containsExactly(exercise);
        verify(exerciseRepository).searchVisibleByUsernameOrderByNameAsc("marko", "press");
    }

    @Test
    void findAllMuscleGroups_returnsRepositoryResult() {
        when(muscleGroupRepository.findAll()).thenReturn(List.of(chest, back));

        List<MuscleGroup> result = exerciseService.findAllMuscleGroups();

        assertThat(result).containsExactly(chest, back);
        verify(muscleGroupRepository).findAll();
    }

    @Test
    void createExercise_setsOwnerSharedFlagAndMuscleGroups() {
        Exercise newExercise = exercise(null, "Push Up", "Bodyweight exercise", ExerciseType.STRENGTH, null, true, Set.of());
        when(appUserRepository.findByUsername("marko")).thenReturn(Optional.of(owner));
        when(muscleGroupRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(chest, back));

        exerciseService.createExercise(newExercise, List.of(10L, 11L), "marko");

        verify(appUserRepository).findByUsername("marko");
        verify(muscleGroupRepository).findAllById(List.of(10L, 11L));
        verify(exerciseRepository).save(exerciseCaptor.capture());

        Exercise saved = exerciseCaptor.getValue();
        assertThat(saved).isSameAs(newExercise);
        assertThat(saved.getOwner()).isSameAs(owner);
        assertThat(saved.isShared()).isFalse();
        assertThat(saved.getMuscleGroups()).containsExactlyInAnyOrder(chest, back);
    }

    @Test
    void createExercise_throwsWhenOwnerDoesNotExist() {
        when(appUserRepository.findByUsername("marko")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exerciseService.createExercise(exercise(null, "Push Up", null, ExerciseType.STRENGTH, null, true, Set.of()), List.of(10L), "marko"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found: marko");

        verify(appUserRepository).findByUsername("marko");
        verifyNoInteractions(muscleGroupRepository, exerciseRepository);
    }

    @Test
    void updateExercise_updatesEditableExerciseAndMuscleGroups() {
        Exercise existing = exercise(100L, "Old", "Old desc", ExerciseType.CARDIO, owner, false, Set.of(chest));
        Exercise updateData = exercise(null, "New name", "New desc", ExerciseType.FLEXIBILITY, null, false, Set.of());
        when(exerciseRepository.findByIdAndOwnerUsername(100L, "marko")).thenReturn(Optional.of(existing));
        when(muscleGroupRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(chest, back));

        exerciseService.updateExercise(100L, updateData, List.of(10L, 11L), "marko", false);

        verify(exerciseRepository).findByIdAndOwnerUsername(100L, "marko");
        verify(muscleGroupRepository).findAllById(List.of(10L, 11L));
        verify(exerciseRepository).save(exerciseCaptor.capture());

        Exercise saved = exerciseCaptor.getValue();
        assertThat(saved).isSameAs(existing);
        assertThat(saved.getName()).isEqualTo("New name");
        assertThat(saved.getDescription()).isEqualTo("New desc");
        assertThat(saved.getType()).isEqualTo(ExerciseType.FLEXIBILITY);
        assertThat(saved.getMuscleGroups()).containsExactlyInAnyOrder(chest, back);
    }

    @Test
    void deleteById_deletesExerciseWhenNotUsedInWorkout() {
        Exercise deletable = exercise(100L, "Bench Press", "Chest exercise", ExerciseType.STRENGTH, owner, true, Set.of(chest, back));
        when(exerciseRepository.findByIdAndOwnerUsername(100L, "marko")).thenReturn(Optional.of(deletable));
        when(workoutExerciseRepository.existsByExerciseId(100L)).thenReturn(false);

        exerciseService.deleteById(100L, "marko", false);

        assertThat(deletable.getMuscleGroups()).isEmpty();
        verify(workoutExerciseRepository).existsByExerciseId(100L);
        verify(exerciseRepository).delete(deletable);
    }

    @Test
    void deleteById_throwsWhenExerciseIsUsedInWorkout() {
        when(exerciseRepository.findByIdAndOwnerUsername(100L, "marko")).thenReturn(Optional.of(exercise));
        when(workoutExerciseRepository.existsByExerciseId(100L)).thenReturn(true);

        assertThatThrownBy(() -> exerciseService.deleteById(100L, "marko", false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Exercise cannot be deleted because it is used in a workout.");

        verify(workoutExerciseRepository).existsByExerciseId(100L);
        verify(exerciseRepository, never()).delete(exercise);
    }

    private static AppUser user(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("hash");
        user.setRole(hr.tvz.gaintrack.model.UserRole.USER);
        return user;
    }

    private static MuscleGroup muscleGroup(Long id, String name) {
        MuscleGroup muscleGroup = new MuscleGroup();
        muscleGroup.setId(id);
        muscleGroup.setName(name);
        return muscleGroup;
    }

    private static Exercise exercise(Long id,
                                     String name,
                                     String description,
                                     ExerciseType type,
                                     AppUser owner,
                                     boolean shared,
                                     Set<MuscleGroup> muscleGroups) {
        Exercise exercise = new Exercise();
        exercise.setId(id);
        exercise.setName(name);
        exercise.setDescription(description);
        exercise.setType(type);
        exercise.setOwner(owner);
        exercise.setShared(shared);
        exercise.setMuscleGroups(muscleGroups instanceof java.util.LinkedHashSet ? muscleGroups : new java.util.LinkedHashSet<>(muscleGroups));
        return exercise;
    }
}



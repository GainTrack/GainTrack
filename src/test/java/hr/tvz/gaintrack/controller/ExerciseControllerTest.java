package hr.tvz.gaintrack.controller;

import hr.tvz.gaintrack.model.AppUser;
import hr.tvz.gaintrack.model.Exercise;
import hr.tvz.gaintrack.model.ExerciseType;
import hr.tvz.gaintrack.model.MuscleGroup;
import hr.tvz.gaintrack.model.UserRole;
import hr.tvz.gaintrack.service.ExerciseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseControllerTest {

    @Mock
    private ExerciseService exerciseService;

    @InjectMocks
    private ExerciseController exerciseController;

    private Exercise exercise;
    private MuscleGroup chest;
    private MuscleGroup back;

    @BeforeEach
    void setUp() {
        AppUser owner = user(1L, "marko");
        chest = muscleGroup(10L, "Chest");
        back = muscleGroup(11L, "Back");
        exercise = exercise(100L, "Bench Press", "Chest exercise", ExerciseType.STRENGTH, owner, true, Set.of(chest, back));
    }

    @Test
    void getExercises_populatesModelAndReturnsIndexView() {
        when(exerciseService.filterExercises("press", null, null, "marko"))
                .thenReturn(List.of(exercise));
        when(exerciseService.findAllMuscleGroups()).thenReturn(List.of(chest, back));
        when(exerciseService.countVisibleExercises("marko")).thenReturn(1);

        Model model = new ExtendedModelMap();
        String view = exerciseController.getExercises("press", null, null, authentication("marko", "ROLE_USER"), model);

        assertThat(view).isEqualTo("exercises/index");
        assertThat(model.getAttribute("exercises")).isEqualTo(List.of(exercise));
        assertThat(model.getAttribute("search")).isEqualTo("press");
        assertThat(model.getAttribute("selectedType")).isNull();
        assertThat(model.getAttribute("selectedMuscleGroupId")).isNull();
        assertThat(model.getAttribute("exerciseTypes")).isEqualTo(ExerciseType.values());
        assertThat(model.getAttribute("muscleGroups")).isEqualTo(List.of(chest, back));
        assertThat(model.getAttribute("totalExercises")).isEqualTo(1);
        assertThat(model.getAttribute("isAdmin")).isEqualTo(false);

        verify(exerciseService).filterExercises("press", null, null, "marko");
        verify(exerciseService).findAllMuscleGroups();
        verify(exerciseService).countVisibleExercises("marko");
    }

    @Test
    void getExercises_marksAdminUsersInModel() {
        Exercise squat = exercise(101L, "Squat", "Leg exercise", ExerciseType.CARDIO, user(2L, "admin"), false, Set.of(back));

        when(exerciseService.filterExercises(null, null, null, "admin"))
                .thenReturn(List.of(squat));
        when(exerciseService.findAllMuscleGroups()).thenReturn(List.of(chest, back));
        when(exerciseService.countVisibleExercises("admin")).thenReturn(1);

        Model model = new ExtendedModelMap();
        String view = exerciseController.getExercises(null, null, null, authentication("admin", "ROLE_ADMIN"), model);

        assertThat(view).isEqualTo("exercises/index");
        assertThat(model.getAttribute("exercises")).isEqualTo(List.of(squat));
        assertThat(model.getAttribute("isAdmin")).isEqualTo(true);

        verify(exerciseService).filterExercises(null, null, null, "admin");
        verify(exerciseService).findAllMuscleGroups();
        verify(exerciseService).countVisibleExercises("admin");
    }

    @Test
    void getExercises_passesFilterParametersToService() {
        when(exerciseService.filterExercises("bench", ExerciseType.STRENGTH, 10L, "marko"))
                .thenReturn(List.of(exercise));
        when(exerciseService.findAllMuscleGroups()).thenReturn(List.of(chest, back));
        when(exerciseService.countVisibleExercises("marko")).thenReturn(5);

        Model model = new ExtendedModelMap();
        String view = exerciseController.getExercises("bench", ExerciseType.STRENGTH, 10L, authentication("marko", "ROLE_USER"), model);

        assertThat(view).isEqualTo("exercises/index");
        assertThat(model.getAttribute("exercises")).isEqualTo(List.of(exercise));
        assertThat(model.getAttribute("search")).isEqualTo("bench");
        assertThat(model.getAttribute("selectedType")).isEqualTo(ExerciseType.STRENGTH);
        assertThat(model.getAttribute("selectedMuscleGroupId")).isEqualTo(10L);
        assertThat(model.getAttribute("totalExercises")).isEqualTo(5);

        verify(exerciseService).filterExercises("bench", ExerciseType.STRENGTH, 10L, "marko");
        verify(exerciseService).findAllMuscleGroups();
        verify(exerciseService).countVisibleExercises("marko");
    }

    @Test
    void showCreateForm_addsFormAttributes() {
        when(exerciseService.findAllMuscleGroups()).thenReturn(List.of(chest, back));

        Model model = new ExtendedModelMap();
        String view = exerciseController.showCreateForm(model);

        assertThat(view).isEqualTo("exercises/create");
        assertThat(model.getAttribute("exercise")).isInstanceOf(Exercise.class);
        assertThat(model.getAttribute("exerciseTypes")).isEqualTo(ExerciseType.values());
        assertThat(model.getAttribute("muscleGroups")).isEqualTo(List.of(chest, back));
        verify(exerciseService).findAllMuscleGroups();
    }

    @Test
    void createExercise_delegatesToServiceAndRedirects() {
        Exercise formExercise = exercise(null, "Push Up", "Bodyweight exercise", ExerciseType.STRENGTH, null, false, Set.of());

        String view = exerciseController.createExercise(formExercise, List.of(10L, 11L), authentication("marko", "ROLE_USER"));

        assertThat(view).isEqualTo("redirect:/exercises");
        verify(exerciseService).createExercise(formExercise, List.of(10L, 11L), "marko");
    }

    @Test
    void showEditForm_populatesModelForEditableExercise() {
        when(exerciseService.getEditableExerciseForForm(100L, "marko", false)).thenReturn(exercise);
        when(exerciseService.findAllMuscleGroups()).thenReturn(List.of(chest, back));

        Model model = new ExtendedModelMap();
        String view = exerciseController.showEditForm(100L, authentication("marko", "ROLE_USER"), model);

        assertThat(view).isEqualTo("exercises/edit");
        assertThat(model.getAttribute("exercise")).isEqualTo(exercise);
        assertThat(model.getAttribute("exerciseTypes")).isEqualTo(ExerciseType.values());
        assertThat(model.getAttribute("muscleGroups")).isEqualTo(List.of(chest, back));
        assertThat(model.getAttribute("selectedMuscleGroupIds")).isEqualTo(Set.of(10L, 11L));
        verify(exerciseService).getEditableExerciseForForm(100L, "marko", false);
        verify(exerciseService).findAllMuscleGroups();
    }

    @Test
    void showEditForm_redirectsWhenExerciseIsNotEditable() {
        when(exerciseService.getEditableExerciseForForm(100L, "marko", false))
                .thenThrow(new IllegalArgumentException("Exercise not found or not accessible."));

        Model model = new ExtendedModelMap();
        String view = exerciseController.showEditForm(100L, authentication("marko", "ROLE_USER"), model);

        assertThat(view).isEqualTo("redirect:/exercises");
        verify(exerciseService).getEditableExerciseForForm(100L, "marko", false);
    }

    @Test
    void editExercise_redirectsAfterSuccessfulUpdate() {
        Exercise formExercise = exercise(null, "Incline Press", "Updated", ExerciseType.STRENGTH, null, false, Set.of());

        String view = exerciseController.editExercise(100L, formExercise, List.of(10L), authentication("marko", "ROLE_USER"));

        assertThat(view).isEqualTo("redirect:/exercises");
        verify(exerciseService).updateExercise(100L, formExercise, List.of(10L), "marko", false);
    }

    @Test
    void editExercise_redirectsWhenUpdateFails() {
        Exercise formExercise = exercise(null, "Incline Press", "Updated", ExerciseType.FLEXIBILITY, null, false, Set.of());
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Exercise not found or not accessible."))
                .when(exerciseService)
                .updateExercise(eq(100L), eq(formExercise), anyList(), eq("marko"), anyBoolean());

        String view = exerciseController.editExercise(100L, formExercise, List.of(10L), authentication("marko", "ROLE_USER"));

        assertThat(view).isEqualTo("redirect:/exercises");
    }

    @Test
    void deleteExercise_addsSuccessFlashMessage() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = exerciseController.deleteExercise(100L, authentication("marko", "ROLE_USER"), redirectAttributes);

        assertThat(view).isEqualTo("redirect:/exercises");
        assertThat(redirectAttributes.getFlashAttributes().get("successMessage")).isEqualTo("Exercise deleted.");
        verify(exerciseService).deleteById(100L, "marko", false);
    }

    @Test
    void deleteExercise_addsErrorFlashMessageWhenExerciseIsInUse() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        org.mockito.Mockito.doThrow(new IllegalStateException("Exercise cannot be deleted because it is used in a workout."))
                .when(exerciseService)
                .deleteById(100L, "marko", false);

        String view = exerciseController.deleteExercise(100L, authentication("marko", "ROLE_USER"), redirectAttributes);

        assertThat(view).isEqualTo("redirect:/exercises");
        assertThat(redirectAttributes.getFlashAttributes().get("errorMessage"))
                .isEqualTo("Exercise cannot be deleted because it is used in a workout.");
    }

    @Test
    void deleteExercise_redirectsWhenExerciseCannotBeAccessed() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Exercise not found or not accessible."))
                .when(exerciseService)
                .deleteById(100L, "marko", false);

        String view = exerciseController.deleteExercise(100L, authentication("marko", "ROLE_USER"), redirectAttributes);

        assertThat(view).isEqualTo("redirect:/exercises");
        assertThat(redirectAttributes.getFlashAttributes()).isEmpty();
    }

    private static Authentication authentication(String username, String authority) {
        return new UsernamePasswordAuthenticationToken(
                username,
                "password",
                List.of(new SimpleGrantedAuthority(authority))
        );
    }

    private static AppUser user(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("hash");
        user.setRole(UserRole.USER);
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
        exercise.setMuscleGroups(new java.util.LinkedHashSet<>(muscleGroups));
        return exercise;
    }
}




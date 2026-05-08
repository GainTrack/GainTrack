package hr.tvz.gaintrack.controller;

import hr.tvz.gaintrack.dto.WorkoutCreate;
import hr.tvz.gaintrack.model.Exercise;
import hr.tvz.gaintrack.model.ExerciseType;
import hr.tvz.gaintrack.model.Workout;
import hr.tvz.gaintrack.service.WorkoutService;
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
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutControllerTest {

    @Mock
    private WorkoutService workoutService;

    @InjectMocks
    private WorkoutController workoutController;

    private Workout workout;
    private Exercise benchPress;

    @BeforeEach
    void setUp() {
        workout = workout(1L, "Push Day", "Chest and triceps workout");
        benchPress = exercise(10L, "Bench Press", ExerciseType.STRENGTH);
    }

    @Test
    void showWorkoutList_populatesModelAndReturnsIndexView() {
        when(workoutService.getAllWorkouts("marko")).thenReturn(List.of(workout));

        Model model = new ExtendedModelMap();
        String view = workoutController.showWorkoutList(authentication("marko", "ROLE_USER"), model);

        assertThat(view).isEqualTo("workouts/index");
        assertThat(model.getAttribute("workouts")).isEqualTo(List.of(workout));
        assertThat(model.getAttribute("isAdmin")).isEqualTo(false);

        verify(workoutService).getAllWorkouts("marko");
    }

    @Test
    void showWorkoutList_marksAdminUsersInModel() {
        when(workoutService.getAllWorkouts("admin")).thenReturn(List.of(workout));

        Model model = new ExtendedModelMap();
        String view = workoutController.showWorkoutList(authentication("admin", "ROLE_ADMIN"), model);

        assertThat(view).isEqualTo("workouts/index");
        assertThat(model.getAttribute("workouts")).isEqualTo(List.of(workout));
        assertThat(model.getAttribute("isAdmin")).isEqualTo(true);

        verify(workoutService).getAllWorkouts("admin");
    }

    @Test
    void showCreateForm_addsEmptyWorkoutCreateAndAvailableExercises() {
        when(workoutService.findAllExercises("marko")).thenReturn(List.of(benchPress));

        Model model = new ExtendedModelMap();
        String view = workoutController.showCreateForm(authentication("marko", "ROLE_USER"), model);

        assertThat(view).isEqualTo("workouts/create");
        assertThat(model.getAttribute("workoutCreate")).isInstanceOf(WorkoutCreate.class);
        assertThat(model.getAttribute("availableExercises")).isEqualTo(List.of(benchPress));

        WorkoutCreate workoutCreate = (WorkoutCreate) model.getAttribute("workoutCreate");
        assertThat(workoutCreate.getExercises()).hasSize(1);
        assertThat(workoutCreate.getExercises().get(0).getSets()).hasSize(1);
        assertThat(workoutCreate.getExercises().get(0).getSets().get(0).getSetNumber()).isEqualTo(1);

        verify(workoutService).findAllExercises("marko");
    }

    @Test
    void createWorkout_redirectsToDetailsWhenCreateSucceeds() {
        WorkoutCreate workoutCreate = validWorkoutCreate();

        when(workoutService.createWorkout(workoutCreate, "marko")).thenReturn(workout);

        BindingResult bindingResult = new BeanPropertyBindingResult(workoutCreate, "workoutCreate");
        Model model = new ExtendedModelMap();

        String view = workoutController.createWorkout(
                workoutCreate,
                bindingResult,
                authentication("marko", "ROLE_USER"),
                model
        );

        assertThat(view).isEqualTo("redirect:/workouts/1");

        verify(workoutService).createWorkout(workoutCreate, "marko");
    }

    @Test
    void createWorkout_returnsCreateViewWhenValidationHasErrors() {
        WorkoutCreate workoutCreate = new WorkoutCreate();
        BindingResult bindingResult = new BeanPropertyBindingResult(workoutCreate, "workoutCreate");
        bindingResult.rejectValue("name", "NotBlank", "Workout name is required.");

        when(workoutService.findAllExercises("marko")).thenReturn(List.of(benchPress));

        Model model = new ExtendedModelMap();

        String view = workoutController.createWorkout(
                workoutCreate,
                bindingResult,
                authentication("marko", "ROLE_USER"),
                model
        );

        assertThat(view).isEqualTo("workouts/create");
        assertThat(model.getAttribute("availableExercises")).isEqualTo(List.of(benchPress));

        verify(workoutService).findAllExercises("marko");
    }

    @Test
    void createWorkout_returnsCreateViewWhenServiceThrowsException() {
        WorkoutCreate workoutCreate = validWorkoutCreate();
        BindingResult bindingResult = new BeanPropertyBindingResult(workoutCreate, "workoutCreate");

        when(workoutService.createWorkout(workoutCreate, "marko"))
                .thenThrow(new IllegalArgumentException("A workout with this name already exists."));
        when(workoutService.findAllExercises("marko")).thenReturn(List.of(benchPress));

        Model model = new ExtendedModelMap();

        String view = workoutController.createWorkout(
                workoutCreate,
                bindingResult,
                authentication("marko", "ROLE_USER"),
                model
        );

        assertThat(view).isEqualTo("workouts/create");
        assertThat(bindingResult.hasGlobalErrors()).isTrue();
        assertThat(model.getAttribute("availableExercises")).isEqualTo(List.of(benchPress));

        verify(workoutService).createWorkout(workoutCreate, "marko");
        verify(workoutService).findAllExercises("marko");
    }

    @Test
    void showWorkoutDetails_populatesModelAndReturnsDetailsView() {
        when(workoutService.getWorkoutById(1L, "marko")).thenReturn(workout);

        Model model = new ExtendedModelMap();
        String view = workoutController.showWorkoutDetails(
                1L,
                authentication("marko", "ROLE_USER"),
                model
        );

        assertThat(view).isEqualTo("workouts/details");
        assertThat(model.getAttribute("workout")).isEqualTo(workout);
        assertThat(model.getAttribute("isAdmin")).isEqualTo(false);

        verify(workoutService).getWorkoutById(1L, "marko");
    }

    @Test
    void showWorkoutDetails_marksAdminUsersInModel() {
        when(workoutService.getWorkoutById(1L, "admin")).thenReturn(workout);

        Model model = new ExtendedModelMap();
        String view = workoutController.showWorkoutDetails(
                1L,
                authentication("admin", "ROLE_ADMIN"),
                model
        );

        assertThat(view).isEqualTo("workouts/details");
        assertThat(model.getAttribute("workout")).isEqualTo(workout);
        assertThat(model.getAttribute("isAdmin")).isEqualTo(true);

        verify(workoutService).getWorkoutById(1L, "admin");
    }

    @Test
    void showEditForm_populatesModelAndReturnsEditView() {
        WorkoutCreate workoutCreate = validWorkoutCreate();

        when(workoutService.getWorkoutFormById(1L, "marko", false)).thenReturn(workoutCreate);
        when(workoutService.findAllExercises("marko")).thenReturn(List.of(benchPress));

        Model model = new ExtendedModelMap();
        String view = workoutController.showEditForm(
                1L,
                authentication("marko", "ROLE_USER"),
                model
        );

        assertThat(view).isEqualTo("workouts/edit");
        assertThat(model.getAttribute("workoutCreate")).isEqualTo(workoutCreate);
        assertThat(model.getAttribute("workoutId")).isEqualTo(1L);
        assertThat(model.getAttribute("availableExercises")).isEqualTo(List.of(benchPress));

        verify(workoutService).getWorkoutFormById(1L, "marko", false);
        verify(workoutService).findAllExercises("marko");
    }

    @Test
    void showEditForm_usesAdminAccessWhenUserIsAdmin() {
        WorkoutCreate workoutCreate = validWorkoutCreate();

        when(workoutService.getWorkoutFormById(1L, "admin", true)).thenReturn(workoutCreate);
        when(workoutService.findAllExercises("admin")).thenReturn(List.of(benchPress));

        Model model = new ExtendedModelMap();
        String view = workoutController.showEditForm(
                1L,
                authentication("admin", "ROLE_ADMIN"),
                model
        );

        assertThat(view).isEqualTo("workouts/edit");
        assertThat(model.getAttribute("workoutCreate")).isEqualTo(workoutCreate);
        assertThat(model.getAttribute("workoutId")).isEqualTo(1L);
        assertThat(model.getAttribute("availableExercises")).isEqualTo(List.of(benchPress));

        verify(workoutService).getWorkoutFormById(1L, "admin", true);
        verify(workoutService).findAllExercises("admin");
    }

    @Test
    void showEditForm_redirectsWhenWorkoutIsNotEditable() {
        when(workoutService.getWorkoutFormById(1L, "marko", false))
                .thenThrow(new IllegalArgumentException("Workout not found or not accessible."));

        Model model = new ExtendedModelMap();
        String view = workoutController.showEditForm(
                1L,
                authentication("marko", "ROLE_USER"),
                model
        );

        assertThat(view).isEqualTo("redirect:/workouts");

        verify(workoutService).getWorkoutFormById(1L, "marko", false);
    }

    @Test
    void editWorkout_redirectsToDetailsWhenUpdateSucceeds() {
        WorkoutCreate workoutCreate = validWorkoutCreate();

        when(workoutService.updateWorkout(1L, workoutCreate, "marko", false)).thenReturn(workout);

        BindingResult bindingResult = new BeanPropertyBindingResult(workoutCreate, "workoutCreate");
        Model model = new ExtendedModelMap();

        String view = workoutController.editWorkout(
                1L,
                workoutCreate,
                bindingResult,
                authentication("marko", "ROLE_USER"),
                model
        );

        assertThat(view).isEqualTo("redirect:/workouts/1");

        verify(workoutService).updateWorkout(1L, workoutCreate, "marko", false);
    }

    @Test
    void editWorkout_usesAdminAccessWhenUserIsAdmin() {
        WorkoutCreate workoutCreate = validWorkoutCreate();

        when(workoutService.updateWorkout(1L, workoutCreate, "admin", true)).thenReturn(workout);

        BindingResult bindingResult = new BeanPropertyBindingResult(workoutCreate, "workoutCreate");
        Model model = new ExtendedModelMap();

        String view = workoutController.editWorkout(
                1L,
                workoutCreate,
                bindingResult,
                authentication("admin", "ROLE_ADMIN"),
                model
        );

        assertThat(view).isEqualTo("redirect:/workouts/1");

        verify(workoutService).updateWorkout(1L, workoutCreate, "admin", true);
    }

    @Test
    void editWorkout_returnsEditViewWhenValidationHasErrors() {
        WorkoutCreate workoutCreate = new WorkoutCreate();
        BindingResult bindingResult = new BeanPropertyBindingResult(workoutCreate, "workoutCreate");
        bindingResult.rejectValue("name", "NotBlank", "Workout name is required.");

        when(workoutService.findAllExercises("marko")).thenReturn(List.of(benchPress));

        Model model = new ExtendedModelMap();

        String view = workoutController.editWorkout(
                1L,
                workoutCreate,
                bindingResult,
                authentication("marko", "ROLE_USER"),
                model
        );

        assertThat(view).isEqualTo("workouts/edit");
        assertThat(model.getAttribute("workoutId")).isEqualTo(1L);
        assertThat(model.getAttribute("availableExercises")).isEqualTo(List.of(benchPress));

        verify(workoutService).findAllExercises("marko");
    }

    @Test
    void editWorkout_returnsEditViewWhenServiceThrowsException() {
        WorkoutCreate workoutCreate = validWorkoutCreate();
        BindingResult bindingResult = new BeanPropertyBindingResult(workoutCreate, "workoutCreate");

        when(workoutService.updateWorkout(1L, workoutCreate, "marko", false))
                .thenThrow(new IllegalArgumentException("A workout with this name already exists."));
        when(workoutService.findAllExercises("marko")).thenReturn(List.of(benchPress));

        Model model = new ExtendedModelMap();

        String view = workoutController.editWorkout(
                1L,
                workoutCreate,
                bindingResult,
                authentication("marko", "ROLE_USER"),
                model
        );

        assertThat(view).isEqualTo("workouts/edit");
        assertThat(bindingResult.hasGlobalErrors()).isTrue();
        assertThat(model.getAttribute("workoutId")).isEqualTo(1L);
        assertThat(model.getAttribute("availableExercises")).isEqualTo(List.of(benchPress));

        verify(workoutService).updateWorkout(1L, workoutCreate, "marko", false);
        verify(workoutService).findAllExercises("marko");
    }

    @Test
    void deleteWorkout_delegatesToServiceAndRedirects() {
        String view = workoutController.deleteWorkout(
                1L,
                authentication("marko", "ROLE_USER")
        );

        assertThat(view).isEqualTo("redirect:/workouts");

        verify(workoutService).deleteById(1L, "marko", false);
    }

    @Test
    void deleteWorkout_usesAdminAccessWhenUserIsAdmin() {
        String view = workoutController.deleteWorkout(
                1L,
                authentication("admin", "ROLE_ADMIN")
        );

        assertThat(view).isEqualTo("redirect:/workouts");

        verify(workoutService).deleteById(1L, "admin", true);
    }

    @Test
    void deleteWorkout_redirectsWhenWorkoutCannotBeAccessed() {
        doThrow(new IllegalArgumentException("Workout not found or not accessible."))
                .when(workoutService)
                .deleteById(1L, "marko", false);

        String view = workoutController.deleteWorkout(
                1L,
                authentication("marko", "ROLE_USER")
        );

        assertThat(view).isEqualTo("redirect:/workouts");

        verify(workoutService).deleteById(1L, "marko", false);
    }

    @Test
    void createExerciseRow_populatesModelAndReturnsFragment() {
        when(workoutService.findAllExercises("marko")).thenReturn(List.of(benchPress));

        Model model = new ExtendedModelMap();
        String view = workoutController.createExerciseRow(
                2,
                authentication("marko", "ROLE_USER"),
                model
        );

        assertThat(view).isEqualTo("workouts/fragments/create-workout-rows :: createWorkoutExerciseRow");
        assertThat(model.getAttribute("exerciseIndex")).isEqualTo(2);
        assertThat(model.getAttribute("exerciseForm")).isNull();
        assertThat(model.getAttribute("availableExercises")).isEqualTo(List.of(benchPress));

        verify(workoutService).findAllExercises("marko");
    }

    @Test
    void createSetRow_populatesModelAndReturnsFragment() {
        Model model = new ExtendedModelMap();
        String view = workoutController.createSetRow(2, 3, model);

        assertThat(view).isEqualTo("workouts/fragments/create-workout-rows :: createWorkoutSetRow");
        assertThat(model.getAttribute("exerciseIndex")).isEqualTo(2);
        assertThat(model.getAttribute("setIndex")).isEqualTo(3);
        assertThat(model.getAttribute("setForm")).isNull();
    }

    private static Authentication authentication(String username, String authority) {
        return new UsernamePasswordAuthenticationToken(
                username,
                "password",
                List.of(new SimpleGrantedAuthority(authority))
        );
    }

    private static Workout workout(Long id, String name, String description) {
        Workout workout = new Workout();
        workout.setId(id);
        workout.setName(name);
        workout.setDescription(description);
        workout.setWorkoutExercises(new LinkedHashSet<>());
        return workout;
    }

    private static Exercise exercise(Long id, String name, ExerciseType type) {
        Exercise exercise = new Exercise();
        exercise.setId(id);
        exercise.setName(name);
        exercise.setDescription(name + " description");
        exercise.setType(type);
        return exercise;
    }

    private static WorkoutCreate validWorkoutCreate() {
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
}
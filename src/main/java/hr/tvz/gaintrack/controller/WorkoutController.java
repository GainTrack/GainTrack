package hr.tvz.gaintrack.controller;

import hr.tvz.gaintrack.dto.WorkoutCreate;
import hr.tvz.gaintrack.model.Workout;
import hr.tvz.gaintrack.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/workouts")
public class WorkoutController {
    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping
    public String showWorkoutList(Model model) {
        model.addAttribute("workouts", workoutService.getAllWorkouts());
        return "workouts/index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("workoutCreate", createEmptyWorkoutCreate());
        model.addAttribute("availableExercises", workoutService.findAllExercises());
        return "workouts/create";
    }

    @PostMapping
    public String createWorkout(@Valid @ModelAttribute("workoutCreate") WorkoutCreate workoutCreate,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("availableExercises", workoutService.findAllExercises());
            return "workouts/create";
        }

        try {
            Workout workout = workoutService.createWorkout(workoutCreate);
            return "redirect:/workouts/" + workout.getId();
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("workout.create.failed", exception.getMessage());
            model.addAttribute("availableExercises", workoutService.findAllExercises());
            return "workouts/create";
        }
    }

    @GetMapping("/{id}")
    public String showWorkoutDetails(@PathVariable Long id, Model model) {
        Workout workout = workoutService.getWorkoutById(id);
        model.addAttribute("workout", workout);
        return "workouts/details";
    }

    @GetMapping("/form/exercise-row")
    public String createExerciseRow(@RequestParam int exerciseIndex, Model model) {
        model.addAttribute("exerciseIndex", exerciseIndex);
        model.addAttribute("exerciseForm", null);
        model.addAttribute("availableExercises", workoutService.findAllExercises());
        return "workouts/fragments/create-workout-rows :: createWorkoutExerciseRow";
    }

    @GetMapping("/form/set-row")
    public String createSetRow(@RequestParam int exerciseIndex,
                               @RequestParam int setIndex,
                               Model model) {
        model.addAttribute("exerciseIndex", exerciseIndex);
        model.addAttribute("setIndex", setIndex);
        model.addAttribute("setForm", null);
        return "workouts/fragments/create-workout-rows :: createWorkoutSetRow";
    }

    private WorkoutCreate createEmptyWorkoutCreate() {
        WorkoutCreate workoutCreate = new WorkoutCreate();

        WorkoutCreate.WorkoutExerciseCreate workoutExerciseCreate = new WorkoutCreate.WorkoutExerciseCreate();
        WorkoutCreate.WorkoutExerciseSetCreate workoutExerciseSetCreate = new WorkoutCreate.WorkoutExerciseSetCreate();
        workoutExerciseSetCreate.setSetNumber(1);
        workoutExerciseCreate.getSets().add(workoutExerciseSetCreate);
        workoutCreate.getExercises().add(workoutExerciseCreate);

        return workoutCreate;
    }
}

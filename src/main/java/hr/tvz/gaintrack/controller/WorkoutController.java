package hr.tvz.gaintrack.controller;

import hr.tvz.gaintrack.model.Workout;
import hr.tvz.gaintrack.model.form.WorkoutForm;
import hr.tvz.gaintrack.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
        return "workouts/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("workoutForm", createEmptyWorkoutForm());
        model.addAttribute("availableExercises", workoutService.findAllExercises());
        return "workouts/form";
    }

    @PostMapping
    public String createWorkout(@Valid @ModelAttribute("workoutForm") WorkoutForm workoutForm,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("availableExercises", workoutService.findAllExercises());
            return "workouts/form";
        }

        try {
            Workout workout = workoutService.createWorkout(workoutForm);
            return "redirect:/workouts/" + workout.getId();
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("workout.create.failed", exception.getMessage());
            model.addAttribute("availableExercises", workoutService.findAllExercises());
            return "workouts/form";
        }
    }

    @GetMapping("/{id}")
    public String showWorkoutDetails(@PathVariable Long id, Model model) {
        Workout workout = workoutService.getWorkoutById(id);
        model.addAttribute("workout", workout);
        return "workouts/details";
    }

    private WorkoutForm createEmptyWorkoutForm() {
        WorkoutForm workoutForm = new WorkoutForm();

        WorkoutForm.WorkoutExerciseForm workoutExerciseForm = new WorkoutForm.WorkoutExerciseForm();
        WorkoutForm.WorkoutExerciseSetForm workoutExerciseSetForm = new WorkoutForm.WorkoutExerciseSetForm();
        workoutExerciseSetForm.setSetNumber(1);
        workoutExerciseForm.getSets().add(workoutExerciseSetForm);
        workoutForm.getExercises().add(workoutExerciseForm);

        return workoutForm;
    }
}

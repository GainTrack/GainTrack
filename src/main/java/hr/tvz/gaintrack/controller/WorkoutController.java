package hr.tvz.gaintrack.controller;

import hr.tvz.gaintrack.model.Workout;
import hr.tvz.gaintrack.service.WorkoutService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @GetMapping("/{id}")
    public String showWorkoutDetails(@PathVariable Long id, Model model) {
        Workout workout = workoutService.getWorkoutById(id);
        model.addAttribute("workout", workout);
        return "workouts/details";
    }
}

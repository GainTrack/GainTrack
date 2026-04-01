package hr.tvz.gaintrack.controller;

import hr.tvz.gaintrack.service.ExerciseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @GetMapping("/exercises")
    public String getExercises(@RequestParam(required = false) String search, Model model) {
        model.addAttribute("exercises", exerciseService.search(search));
        model.addAttribute("search", search);
        return "exercises/list";
    }

    @PostMapping("/exercises/{id}/delete")
    public String deleteExercise(@PathVariable Long id) {
        exerciseService.deleteById(id);
        return "redirect:/exercises";
    }
}
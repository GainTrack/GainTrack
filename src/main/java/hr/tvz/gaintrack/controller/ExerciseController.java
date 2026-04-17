package hr.tvz.gaintrack.controller;

import hr.tvz.gaintrack.model.Exercise;
import hr.tvz.gaintrack.model.ExerciseType;
import hr.tvz.gaintrack.model.MuscleGroup;
import hr.tvz.gaintrack.service.ExerciseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.stream.Collectors;

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
        return "exercises/index";
    }

    @GetMapping("/exercises/new")
    public String showCreateForm(Model model) {
        model.addAttribute("exercise", new Exercise());
        model.addAttribute("exerciseTypes", ExerciseType.values());
        model.addAttribute("muscleGroups", exerciseService.findAllMuscleGroups());
        return "exercises/create";
    }

    @PostMapping("/exercises")
    public String createExercise(@ModelAttribute Exercise exercise,
                                 @RequestParam(required = false) List<Long> muscleGroupIds) {
        exerciseService.createExercise(exercise, muscleGroupIds);
        return "redirect:/exercises";
    }

    @GetMapping("/exercises/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Exercise exercise = exerciseService.getExerciseById(id);
        model.addAttribute("exercise", exercise);
        model.addAttribute("exerciseTypes", ExerciseType.values());
        model.addAttribute("muscleGroups", exerciseService.findAllMuscleGroups());
        model.addAttribute(
                "selectedMuscleGroupIds",
                exercise.getMuscleGroups().stream().map(MuscleGroup::getId).collect(Collectors.toSet())
        );
        return "exercises/edit";
    }

    @PostMapping("/exercises/{id}/edit")
    public String editExercise(@PathVariable Long id,
                               @ModelAttribute Exercise exercise,
                               @RequestParam(required = false) List<Long> muscleGroupIds) {
        exerciseService.updateExercise(id, exercise, muscleGroupIds);
        return "redirect:/exercises";
    }

    @PostMapping("/exercises/{id}/delete")
    public String deleteExercise(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            exerciseService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Exercise deleted.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/exercises";
    }
}

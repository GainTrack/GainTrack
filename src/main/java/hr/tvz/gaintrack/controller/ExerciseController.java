package hr.tvz.gaintrack.controller;

import hr.tvz.gaintrack.model.Exercise;
import hr.tvz.gaintrack.model.ExerciseType;
import hr.tvz.gaintrack.model.MuscleGroup;
import hr.tvz.gaintrack.service.ExerciseService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    public String getExercises(@RequestParam(required = false) String search,
                               @RequestParam(required = false) ExerciseType type,
                               @RequestParam(required = false) Long muscleGroupId,
                               Authentication authentication,
                               Model model) {
        String username = authentication.getName();

        model.addAttribute("exercises",
                exerciseService.filterExercises(search, type, muscleGroupId, username));

        model.addAttribute("search", search);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedMuscleGroupId", muscleGroupId);
        model.addAttribute("exerciseTypes", ExerciseType.values());
        model.addAttribute("muscleGroups", exerciseService.findAllMuscleGroups());
        model.addAttribute("totalExercises", exerciseService.countVisibleExercises(username));
        model.addAttribute("isAdmin", isAdmin(authentication));

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
                                 @RequestParam(required = false) List<Long> muscleGroupIds,
                                 Authentication authentication) {
        exerciseService.createExercise(exercise, muscleGroupIds, authentication.getName());
        return "redirect:/exercises";
    }

    @GetMapping("/exercises/{id}/edit")
    public String showEditForm(@PathVariable Long id, Authentication authentication, Model model) {
        try {
            Exercise exercise = exerciseService.getEditableExerciseForForm(
                    id,
                    authentication.getName(),
                    isAdmin(authentication)
            );

            model.addAttribute("exercise", exercise);
            model.addAttribute("exerciseTypes", ExerciseType.values());
            model.addAttribute("muscleGroups", exerciseService.findAllMuscleGroups());
            model.addAttribute(
                    "selectedMuscleGroupIds",
                    exercise.getMuscleGroups().stream().map(MuscleGroup::getId).collect(Collectors.toSet())
            );
            return "exercises/edit";
        } catch (IllegalArgumentException exception) {
            return "redirect:/exercises";
        }
    }

    @PostMapping("/exercises/{id}/edit")
    public String editExercise(@PathVariable Long id,
                               @ModelAttribute Exercise exercise,
                               @RequestParam(required = false) List<Long> muscleGroupIds,
                               Authentication authentication) {
        try {
            exerciseService.updateExercise(
                    id,
                    exercise,
                    muscleGroupIds,
                    authentication.getName(),
                    isAdmin(authentication)
            );
        } catch (IllegalArgumentException exception) {
            return "redirect:/exercises";
        }

        return "redirect:/exercises";
    }

    @PostMapping("/exercises/{id}/delete")
    public String deleteExercise(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            exerciseService.deleteById(id, authentication.getName(), isAdmin(authentication));
            redirectAttributes.addFlashAttribute("successMessage", "Exercise deleted.");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        } catch (IllegalArgumentException exception) {
            return "redirect:/exercises";
        }

        return "redirect:/exercises";
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMIN") || authority.equals("ADMIN"));
    }
}

package hr.tvz.gaintrack.controller;

import hr.tvz.gaintrack.dto.RegistrationCreate;
import hr.tvz.gaintrack.service.RegistrationException;
import hr.tvz.gaintrack.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.ui.Model;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final RegistrationService registrationService;

    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/login")
    public String showLoginPage(Authentication authentication) {
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }

        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Authentication authentication, Model model) {
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }

        if (!model.containsAttribute("registrationCreate")) {
            model.addAttribute("registrationCreate", new RegistrationCreate());
        }

        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationCreate") RegistrationCreate registrationCreate,
                           BindingResult bindingResult,
                           Authentication authentication) {
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }

        if (!registrationCreate.getPassword().equals(registrationCreate.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Passwords do not match.");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            registrationService.register(registrationCreate);
        } catch (RegistrationException ex) {
            bindingResult.rejectValue(ex.getField(), "duplicate", ex.getMessage());
            return "auth/register";
        }

        return "redirect:/login?registered";
    }
}


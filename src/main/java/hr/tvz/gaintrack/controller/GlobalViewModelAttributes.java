package hr.tvz.gaintrack.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalViewModelAttributes {

    @ModelAttribute("activePage")
    public String activePage(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        String uri = request.getRequestURI();
        if (uri == null || uri.isBlank() || "/".equals(uri)) {
            return "dashboard";
        }
        if (uri.startsWith("/exercises")) {
            return "exercises";
        }
        if (uri.startsWith("/workouts")) {
            return "workouts";
        }
        if (uri.startsWith("/admin/users")) {
            return "adminUsers";
        }

        return "";
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}

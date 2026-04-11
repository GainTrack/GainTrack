package hr.tvz.gaintrack.controller;

import jakarta.servlet.http.HttpServletRequest;
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

        return "";
    }
}


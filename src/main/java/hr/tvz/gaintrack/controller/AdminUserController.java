package hr.tvz.gaintrack.controller;

import hr.tvz.gaintrack.dto.AdminUsersPage;
import hr.tvz.gaintrack.service.AdminUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public String showUsers(@RequestParam(required = false) String search,
                            @RequestParam(required = false) String role,
                            @RequestParam(required = false) String status,
                            Model model) {
        AdminUsersPage usersPage = adminUserService.getUsersPage(search, role, status);

        model.addAttribute("users", usersPage.users());
        model.addAttribute("summary", usersPage.summary());
        model.addAttribute("search", search);
        model.addAttribute("role", role);
        model.addAttribute("status", status);

        return "admin/users";
    }
}

package hr.tvz.gaintrack.service;

import hr.tvz.gaintrack.dto.AdminUserSummary;
import hr.tvz.gaintrack.dto.AdminUsersPage;
import hr.tvz.gaintrack.model.UserRole;
import hr.tvz.gaintrack.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AdminUserService {

    private final AppUserRepository appUserRepository;

    public AdminUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public AdminUsersPage getUsersPage(String search, String role, String status) {
        String searchPattern = normalizeSearchPattern(search);
        UserRole parsedRole = parseRole(role);
        Boolean enabled = parseEnabled(status);

        AdminUserSummary summary = new AdminUserSummary(
                appUserRepository.count(),
                appUserRepository.countByRole(UserRole.ADMIN),
                appUserRepository.countByEnabled(true),
                appUserRepository.countByEnabled(false)
        );

        return new AdminUsersPage(
                appUserRepository.findAdminUserListItems(searchPattern, parsedRole, enabled),
                summary
        );
    }

    private String normalizeSearchPattern(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }

        return "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private UserRole parseRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }

        try {
            return UserRole.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private Boolean parseEnabled(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        if ("enabled".equalsIgnoreCase(status)) {
            return true;
        }
        if ("disabled".equalsIgnoreCase(status)) {
            return false;
        }

        return null;
    }
}

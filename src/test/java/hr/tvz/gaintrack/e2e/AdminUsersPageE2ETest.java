package hr.tvz.gaintrack.e2e;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import hr.tvz.gaintrack.model.AppUser;
import hr.tvz.gaintrack.model.UserRole;
import hr.tvz.gaintrack.model.Workout;
import hr.tvz.gaintrack.repository.AppUserRepository;
import hr.tvz.gaintrack.repository.WorkoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdminUsersPageE2ETest extends BasePageE2ETest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        workoutRepository.deleteAll();
        appUserRepository.deleteAll();

        AppUser admin = appUser("admin", UserRole.ADMIN, true);
        AppUser ana = appUser("ana", UserRole.USER, true);
        AppUser marko = appUser("marko", UserRole.USER, true);
        AppUser disabled = appUser("disabled", UserRole.USER, false);
        appUserRepository.saveAll(List.of(admin, ana, marko, disabled));

        workoutRepository.save(workout("Ana Push", ana));
        workoutRepository.save(workout("Ana Pull", ana));
        workoutRepository.save(workout("Marko Legs", marko));
    }

    @Test
    void adminUsers_asAdminCanOpenPageAndFilterUsers() {
        try (BrowserContext context = browser.newContext();
             Page page = context.newPage()) {
            login(page, "admin", "password123");
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Admin")).click();

            assertThat(page.url()).endsWith("/admin/users");
            assertThat(page.textContent("body"))
                    .contains("Total users", "4")
                    .contains("Admins", "1")
                    .contains("Enabled", "3")
                    .contains("Disabled", "1");

            String initialTableText = page.locator("table").textContent();
            assertThat(initialTableText)
                    .contains("admin", "ana", "marko", "disabled")
                    .contains("admin@example.com", "ana@example.com", "marko@example.com", "disabled@example.com")
                    .contains("ADMIN", "USER");

            page.getByPlaceholder("Search by username or email...").fill("example.com");
            page.getByLabel("Filter by role").selectOption("USER");
            page.getByLabel("Filter by status").selectOption("enabled");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();

            assertThat(page.url())
                    .contains("search=example.com")
                    .contains("role=USER")
                    .contains("status=enabled");

            String filteredTableText = page.locator("table").textContent();
            assertThat(filteredTableText)
                    .contains("ana", "marko")
                    .doesNotContain("admin@example.com", "disabled@example.com");
        }
    }

    private void login(Page page, String username, String password) {
        page.navigate(url("/login"));
        page.getByLabel("Username").fill(username);
        page.getByLabel("Password").fill(password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log in")).click();
    }

    private AppUser appUser(String username, UserRole role, boolean enabled) {
        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setEmail(username + "@example.com");
        appUser.setPasswordHash(passwordEncoder.encode("password123"));
        appUser.setRole(role);
        appUser.setEnabled(enabled);
        appUser.setCreatedAt(LocalDateTime.now());
        return appUser;
    }

    private static Workout workout(String name, AppUser owner) {
        Workout workout = new Workout();
        workout.setName(name);
        workout.setDescription(name + " description");
        workout.setOwner(owner);
        workout.setShared(false);
        return workout;
    }
}

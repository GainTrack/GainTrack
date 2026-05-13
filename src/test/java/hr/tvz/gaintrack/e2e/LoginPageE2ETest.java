package hr.tvz.gaintrack.e2e;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import hr.tvz.gaintrack.model.AppUser;
import hr.tvz.gaintrack.model.UserRole;
import hr.tvz.gaintrack.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LoginPageE2ETest extends BasePageE2ETest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        appUserRepository.deleteAll();
        appUserRepository.save(user("marko", "password123"));
    }

    @Test
    void login_withValidCredentialsRedirectsHomeAndShowsNavbar() {
        try (BrowserContext context = browser.newContext();
             Page page = context.newPage()) {
            login(page, "marko", "password123");

            assertThat(page.url()).endsWith("/");
            assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Dashboard")).isVisible()).isTrue();
            assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Exercises")).isVisible()).isTrue();
            assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Workouts")).isVisible()).isTrue();
            assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Logout")).isVisible()).isTrue();
        }
    }

    @Test
    void login_withBadCredentialsShowsErrorMessage() {
        try (BrowserContext context = browser.newContext();
             Page page = context.newPage()) {
            login(page, "marko", "wrong-password");

            assertThat(page.url()).endsWith("/login?error");
            assertThat(page.getByRole(AriaRole.ALERT).textContent()).contains("Invalid username or password.");
        }
    }

    @Test
    void logout_afterLoginShowsLogoutMessage() {
        try (BrowserContext context = browser.newContext();
             Page page = context.newPage()) {
            login(page, "marko", "password123");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Logout")).click();

            assertThat(page.url()).endsWith("/login?logout");
            assertThat(page.getByRole(AriaRole.ALERT).textContent()).contains("You have been logged out.");
        }
    }

    private void login(Page page, String username, String password) {
        page.navigate(url("/login"));
        page.getByLabel("Username").fill(username);
        page.getByLabel("Password").fill(password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log in")).click();
    }

    private AppUser user(String username, String password) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}

package hr.tvz.gaintrack.e2e;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import hr.tvz.gaintrack.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterPageE2ETest extends BasePageE2ETest {

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void setUp() {
        appUserRepository.deleteAll();
    }

    @Test
    void register_createsAccountAndShowsLoginSuccessMessage() {
        try (BrowserContext context = browser.newContext();
             Page page = context.newPage()) {
            page.navigate(url("/register"));
            page.getByLabel("Username").fill("marko");
            page.getByLabel("Email").fill("marko@example.com");
            page.getByLabel("Password", new Page.GetByLabelOptions().setExact(true)).fill("password123");
            page.getByLabel("Confirm password").fill("password123");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create account")).click();

            assertThat(page.url()).endsWith("/login?registered");
            assertThat(page.getByRole(AriaRole.ALERT).textContent()).contains("Account created successfully");
            assertThat(appUserRepository.existsByUsernameIgnoreCase("marko")).isTrue();
        }
    }
}

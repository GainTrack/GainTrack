package hr.tvz.gaintrack.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import hr.tvz.gaintrack.repository.AppUserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Tag("e2e")
class RegisterPageE2ETest {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:18");

    private static Playwright playwright;
    private static Browser browser;

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(POSTGRES_IMAGE);

    @LocalServerPort
    private int port;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeAll
    static void startBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @BeforeEach
    void setUp() {
        appUserRepository.deleteAll();
    }

    @AfterAll
    static void stopBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @Test
    void register_createsAccountAndShowsLoginSuccessMessage() {
        try (BrowserContext context = browser.newContext();
             Page page = context.newPage()) {
            page.navigate("http://localhost:" + port + "/register");
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

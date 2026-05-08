package hr.tvz.gaintrack.e2e;

import hr.tvz.gaintrack.model.AppUser;
import hr.tvz.gaintrack.model.UserRole;
import hr.tvz.gaintrack.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RegistrationFlowE2ETest {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:18");

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(POSTGRES_IMAGE);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        appUserRepository.deleteAll();
    }

    @Test
    void register_withValidDataCreatesUserAndRedirectsToLogin() throws Exception {
        mockMvc.perform(registrationPost()
                        .param("username", "  Marko  ")
                        .param("email", "MARKO@EXAMPLE.COM")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        Optional<AppUser> createdUser = appUserRepository.findByUsername("Marko");
        assertThat(createdUser).isPresent();
        assertThat(createdUser.get().getEmail()).isEqualTo("marko@example.com");
        assertThat(createdUser.get().getRole()).isEqualTo(UserRole.USER);
        assertThat(createdUser.get().isEnabled()).isTrue();
        assertThat(createdUser.get().getCreatedAt()).isNotNull();
        assertThat(passwordEncoder.matches("password123", createdUser.get().getPasswordHash())).isTrue();
    }

    @Test
    void register_withDuplicateUsernameReturnsRegisterPageAndDoesNotCreateDuplicateUser() throws Exception {
        AppUser existingUser = user("marko", "marko@example.com");
        appUserRepository.save(existingUser);

        mockMvc.perform(registrationPost()
                        .param("username", "MARKO")
                        .param("email", "other@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registrationCreate", "username"));

        assertThat(appUserRepository.count()).isEqualTo(1);
    }

    @Test
    void register_withPasswordMismatchReturnsRegisterPageAndDoesNotCreateUser() throws Exception {
        mockMvc.perform(registrationPost()
                        .param("username", "marko")
                        .param("email", "marko@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "different123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registrationCreate", "confirmPassword"));

        assertThat(appUserRepository.count()).isZero();
    }

    private MockHttpServletRequestBuilder registrationPost() throws Exception {
        MvcResult formResult = mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andReturn();
        CsrfToken csrfToken = (CsrfToken) formResult.getRequest().getAttribute(CsrfToken.class.getName());

        return post("/register")
                .session((org.springframework.mock.web.MockHttpSession) formResult.getRequest().getSession(false))
                .param(csrfToken.getParameterName(), csrfToken.getToken());
    }

    private AppUser user(String username, String email) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setCreatedAt(java.time.LocalDateTime.now());
        return user;
    }
}

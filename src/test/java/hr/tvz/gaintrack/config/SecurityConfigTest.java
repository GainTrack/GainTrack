package hr.tvz.gaintrack.config;

import hr.tvz.gaintrack.model.AppUser;
import hr.tvz.gaintrack.model.UserRole;
import hr.tvz.gaintrack.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
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

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SecurityConfigTest {

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
        appUserRepository.save(user("admin", UserRole.ADMIN, true));
        appUserRepository.save(user("marko", UserRole.USER, true));
    }

    @Test
    void anonymousUsersCanAccessAuthPages() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }

    @Test
    void anonymousUsersAreRedirectedToLoginForProtectedPages() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void regularUsersCannotAccessAdminPages() throws Exception {
        MockHttpSession userSession = login("marko", "password123");

        mockMvc.perform(get("/admin/users").session(userSession))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminUsersCanAccessAdminPages() throws Exception {
        MockHttpSession adminSession = login("admin", "password123");

        mockMvc.perform(get("/admin/users").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"));
    }

    @Test
    void authenticatedUsersAreRedirectedAwayFromAuthPages() throws Exception {
        MockHttpSession userSession = login("marko", "password123");

        mockMvc.perform(get("/login").session(userSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        mockMvc.perform(get("/register").session(userSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    private MockHttpSession login(String username, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(loginPost(username, password))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        return (MockHttpSession) loginResult.getRequest().getSession(false);
    }

    private MockHttpServletRequestBuilder loginPost(String username, String password) throws Exception {
        MvcResult formResult = mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andReturn();
        CsrfToken csrfToken = (CsrfToken) formResult.getRequest().getAttribute(CsrfToken.class.getName());

        return post("/login")
                .session((MockHttpSession) formResult.getRequest().getSession(false))
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .param("username", username)
                .param("password", password);
    }

    private AppUser user(String username, UserRole role, boolean enabled) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setRole(role);
        user.setEnabled(enabled);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}

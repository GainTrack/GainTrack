package hr.tvz.gaintrack.controller;

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
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class LoginFlowTest {

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
        appUserRepository.save(user("marko", UserRole.USER, true));
        appUserRepository.save(user("disabled", UserRole.USER, false));
    }

    @Test
    void login_withValidCredentialsAuthenticatesSessionAndRedirectsHome() throws Exception {
        MockHttpSession session = (MockHttpSession) mockMvc.perform(loginPost("marko", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn()
                .getRequest()
                .getSession(false);

        assertThat(session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY)).isNotNull();

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void login_withBadCredentialsRedirectsToLoginErrorAndDoesNotAuthenticateSession() throws Exception {
        MockHttpSession session = (MockHttpSession) mockMvc.perform(loginPost("marko", "wrong-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andReturn()
                .getRequest()
                .getSession(false);

        assertThat(session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY)).isNull();
    }

    @Test
    void login_withDisabledUserRedirectsToLoginErrorAndDoesNotAuthenticateSession() throws Exception {
        MockHttpSession session = (MockHttpSession) mockMvc.perform(loginPost("disabled", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andReturn()
                .getRequest()
                .getSession(false);

        assertThat(session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY)).isNull();
    }

    @Test
    void logoutClearsAuthenticatedSessionAndRedirectsToLoginLogout() throws Exception {
        MockHttpSession session = (MockHttpSession) mockMvc.perform(loginPost("marko", "password123"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);

        CsrfToken csrfToken = (CsrfToken) mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getAttribute(CsrfToken.class.getName());

        mockMvc.perform(post("/logout")
                        .session(session)
                        .param(csrfToken.getParameterName(), csrfToken.getToken()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
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

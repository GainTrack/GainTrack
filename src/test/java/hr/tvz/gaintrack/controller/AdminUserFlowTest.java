package hr.tvz.gaintrack.controller;

import hr.tvz.gaintrack.dto.AdminUserListItem;
import hr.tvz.gaintrack.dto.AdminUserSummary;
import hr.tvz.gaintrack.model.AppUser;
import hr.tvz.gaintrack.model.UserRole;
import hr.tvz.gaintrack.model.Workout;
import hr.tvz.gaintrack.repository.AppUserRepository;
import hr.tvz.gaintrack.repository.WorkoutRepository;
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
import java.util.List;

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
class AdminUserFlowTest {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:18");

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(POSTGRES_IMAGE);

    @Autowired
    private MockMvc mockMvc;

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
    void showUsers_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void showUsers_forbidsRegularUsers() throws Exception {
        MockHttpSession userSession = login("marko", "password123");

        mockMvc.perform(get("/admin/users").session(userSession))
                .andExpect(status().isForbidden());
    }

    @Test
    void showUsers_asAdminReturnsFilteredUsersAndSummary() throws Exception {
        MockHttpSession adminSession = login("admin", "password123");

        MvcResult result = mockMvc.perform(get("/admin/users")
                        .session(adminSession)
                        .param("search", "example.com")
                        .param("role", "USER")
                        .param("status", "enabled"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attribute("search", "example.com"))
                .andExpect(model().attribute("role", "USER"))
                .andExpect(model().attribute("status", "enabled"))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<AdminUserListItem> users = (List<AdminUserListItem>) result.getModelAndView().getModel().get("users");
        AdminUserSummary summary = (AdminUserSummary) result.getModelAndView().getModel().get("summary");

        assertThat(users)
                .extracting(AdminUserListItem::username)
                .containsExactly("ana", "marko");
        assertThat(workoutCountFor(users, "ana")).isEqualTo(2);
        assertThat(workoutCountFor(users, "marko")).isEqualTo(1);
        assertThat(summary.totalUsers()).isEqualTo(4);
        assertThat(summary.adminUsers()).isEqualTo(1);
        assertThat(summary.enabledUsers()).isEqualTo(3);
        assertThat(summary.disabledUsers()).isEqualTo(1);
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

    private static long workoutCountFor(List<AdminUserListItem> users, String username) {
        return users.stream()
                .filter(user -> user.username().equals(username))
                .findFirst()
                .orElseThrow()
                .workoutCount();
    }
}

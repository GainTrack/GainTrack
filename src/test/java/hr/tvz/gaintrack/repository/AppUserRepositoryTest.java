package hr.tvz.gaintrack.repository;

import hr.tvz.gaintrack.dto.AdminUserListItem;
import hr.tvz.gaintrack.model.AppUser;
import hr.tvz.gaintrack.model.UserRole;
import hr.tvz.gaintrack.model.Workout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AppUserRepositoryTest {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:18");

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(POSTGRES_IMAGE);

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    private AppUser admin;
    private AppUser ana;
    private AppUser marko;
    private AppUser zed;

    @BeforeEach
    void setUp() {
        workoutRepository.deleteAll();
        appUserRepository.deleteAll();

        admin = appUser("admin", "admin@example.com", UserRole.ADMIN, true);
        ana = appUser("ana", "ana@example.com", UserRole.USER, true);
        marko = appUser("marko", "marko@example.com", UserRole.USER, true);
        zed = appUser("zed", "zed@example.com", UserRole.USER, false);
        appUserRepository.saveAll(List.of(marko, admin, zed, ana));

        workoutRepository.save(workout("Ana Push", ana));
        workoutRepository.save(workout("Ana Pull", ana));
        workoutRepository.save(workout("Marko Legs", marko));
    }

    @Test
    void findAdminUserListItems_returnsUsersOrderedByUsernameWithWorkoutCounts() {
        List<AdminUserListItem> result = appUserRepository.findAdminUserListItems(null, null, null);

        assertThat(result)
                .extracting(AdminUserListItem::username)
                .containsExactly("admin", "ana", "marko", "zed");
        assertThat(workoutCountFor(result, "admin")).isZero();
        assertThat(workoutCountFor(result, "ana")).isEqualTo(2);
        assertThat(workoutCountFor(result, "marko")).isEqualTo(1);
        assertThat(workoutCountFor(result, "zed")).isZero();
    }

    @Test
    void findAdminUserListItems_filtersBySearchAcrossUsernameAndEmail() {
        List<AdminUserListItem> result = appUserRepository.findAdminUserListItems("%example.com%", UserRole.USER, true);

        assertThat(result)
                .extracting(AdminUserListItem::username)
                .containsExactly("ana", "marko");
    }

    @Test
    void findAdminUserListItems_filtersByRoleAndEnabledStatus() {
        List<AdminUserListItem> admins = appUserRepository.findAdminUserListItems(null, UserRole.ADMIN, null);
        List<AdminUserListItem> disabledUsers = appUserRepository.findAdminUserListItems(null, UserRole.USER, false);

        assertThat(admins)
                .extracting(AdminUserListItem::username)
                .containsExactly("admin");
        assertThat(disabledUsers)
                .extracting(AdminUserListItem::username)
                .containsExactly("zed");
    }

    private static long workoutCountFor(List<AdminUserListItem> users, String username) {
        return users.stream()
                .filter(user -> user.username().equals(username))
                .findFirst()
                .orElseThrow()
                .workoutCount();
    }

    private static AppUser appUser(String username, String email, UserRole role, boolean enabled) {
        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setEmail(email);
        appUser.setPasswordHash("{noop}password123");
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

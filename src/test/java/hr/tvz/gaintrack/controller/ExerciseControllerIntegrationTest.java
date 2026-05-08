package hr.tvz.gaintrack.controller;

import hr.tvz.gaintrack.model.AppUser;
import hr.tvz.gaintrack.model.Exercise;
import hr.tvz.gaintrack.model.ExerciseType;
import hr.tvz.gaintrack.model.MuscleGroup;
import hr.tvz.gaintrack.model.UserRole;
import hr.tvz.gaintrack.repository.AppUserRepository;
import hr.tvz.gaintrack.repository.ExerciseRepository;
import hr.tvz.gaintrack.repository.MuscleGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ExerciseControllerIntegrationTest {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:18");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private MuscleGroupRepository muscleGroupRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @BeforeEach
    void setUp() {
        exerciseRepository.deleteAll();
        muscleGroupRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void getExercises_shouldReturnExerciseListAndRenderTemplate() throws Exception {
        AppUser user = persistUser("marko");
        MuscleGroup chest = persistMuscleGroup("Chest");
        persistExercise("Bench Press", "Chest exercise", ExerciseType.STRENGTH, user, Set.of(chest));

        mockMvc.perform(get("/exercises").with(user("marko").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("exercises/index"))
                .andExpect(content().string(containsString("Bench Press")));
    }

    @Test
    void showCreateForm_shouldRenderFormWithMuscleGroups() throws Exception {
        persistMuscleGroup("Chest");

        mockMvc.perform(get("/exercises/new").with(user("marko").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("exercises/create"))
                .andExpect(content().string(containsString("Chest")));
    }

    @Test
    void createExercise_shouldPersistExerciseAndRedirect() throws Exception {
        persistUser("marko");
        MuscleGroup chest = persistMuscleGroup("Chest");

        mockMvc.perform(post("/exercises")
                        .with(user("marko").roles("USER"))
                        .with(csrf())
                        .param("name", "Push Up")
                        .param("type", ExerciseType.STRENGTH.name())
                        .param("muscleGroupIds", String.valueOf(chest.getId()))
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exercises"));

        var saved = exerciseRepository.findAll();
        assertThat(saved).anyMatch(e -> e.getName().equals("Push Up") && e.getOwner().getUsername().equals("marko"));
    }

    @Test
    void showEditForm_shouldRenderEditTemplateWithExerciseData() throws Exception {
        AppUser user = persistUser("marko");
        MuscleGroup chest = persistMuscleGroup("Chest");
        Exercise exercise = persistExercise("Bench Press", "Chest exercise", ExerciseType.STRENGTH, user, Set.of(chest));

        mockMvc.perform(get("/exercises/{id}/edit", exercise.getId()).with(user("marko").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("exercises/edit"))
                .andExpect(content().string(containsString("Bench Press")));
    }

    @Test
    void editExercise_shouldUpdateExerciseAndRedirect() throws Exception {
        AppUser user = persistUser("marko");
        MuscleGroup chest = persistMuscleGroup("Chest");
        MuscleGroup back = persistMuscleGroup("Back");
        Exercise exercise = persistExercise("Bench Press", "Original description", ExerciseType.STRENGTH, user, Set.of(chest));

        mockMvc.perform(post("/exercises/{id}/edit", exercise.getId())
                        .with(user("marko").roles("USER"))
                        .with(csrf())
                        .param("name", "Incline Bench Press")
                        .param("description", "Updated description")
                        .param("type", ExerciseType.STRENGTH.name())
                        .param("muscleGroupIds", String.valueOf(back.getId()))
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exercises"));

        var updated = exerciseRepository.findVisibleByIdAndUsername(exercise.getId(), "marko").orElseThrow();
        assertThat(updated.getName()).isEqualTo("Incline Bench Press");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getMuscleGroups()).extracting("id").contains(back.getId());
    }

    @Test
    void deleteExercise_shouldRemoveExerciseAndRedirect() throws Exception {
        AppUser user = persistUser("marko");
        MuscleGroup chest = persistMuscleGroup("Chest");
        Exercise exercise = persistExercise("Bench Press", "Chest exercise", ExerciseType.STRENGTH, user, Set.of(chest));

        mockMvc.perform(post("/exercises/{id}/delete", exercise.getId())
                        .with(user("marko").roles("USER"))
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exercises"));

        assertThat(exerciseRepository.findById(exercise.getId())).isEmpty();
    }

    @Test
    void userCannotEditExerciseOfAnotherUser() throws Exception {
        persistUser("marko");
        AppUser ana = persistUser("ana");
        Exercise exercise = persistExercise("Bench Press", "Ana's exercise", ExerciseType.STRENGTH, ana, Set.of());

        mockMvc.perform(get("/exercises/{id}/edit", exercise.getId()).with(user("marko").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exercises"));
    }

    private AppUser persistUser(String username) {
        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setEmail(username + "@example.com");
        appUser.setPasswordHash("hash");
        appUser.setRole(UserRole.USER);
        appUser.setEnabled(true);
        appUser.setCreatedAt(LocalDateTime.now());
        return appUserRepository.save(appUser);
    }

    private MuscleGroup persistMuscleGroup(String name) {
        MuscleGroup muscleGroup = new MuscleGroup();
        muscleGroup.setName(name);
        return muscleGroupRepository.save(muscleGroup);
    }

    private Exercise persistExercise(String name,
                                     String description,
                                     ExerciseType type,
                                     AppUser owner,
                                     Set<MuscleGroup> muscleGroups) {
        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setDescription(description);
        exercise.setType(type);
        exercise.setOwner(owner);
        exercise.setShared(false);
        exercise.setMuscleGroups(new LinkedHashSet<>(muscleGroups));
        return exerciseRepository.save(exercise);
    }
}

package hr.tvz.gaintrack.repository;

import hr.tvz.gaintrack.model.AppUser;
import hr.tvz.gaintrack.model.Exercise;
import hr.tvz.gaintrack.model.ExerciseType;
import hr.tvz.gaintrack.model.UserRole;
import hr.tvz.gaintrack.model.Workout;
import hr.tvz.gaintrack.model.WorkoutExercise;
import hr.tvz.gaintrack.model.WorkoutExerciseSet;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class WorkoutRepositoryTest {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:18");

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(POSTGRES_IMAGE);

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void existsByNameIgnoreCase_returnsTrueIgnoringCase() {
        AppUser owner = persistUser("repo_exists_owner");
        persistWorkout("Repo Push Day", "Chest workout", owner, false);

        flushAndClear();

        boolean result = workoutRepository.existsByNameIgnoreCase("repo push day");

        assertThat(result).isTrue();
    }

    @Test
    void existsByNameIgnoreCaseAndIdNot_ignoresCurrentWorkoutId() {
        AppUser owner = persistUser("repo_exists_id_owner");
        Workout workout = persistWorkout("Repo Unique Workout", "Workout description", owner, false);

        flushAndClear();

        boolean sameWorkoutResult = workoutRepository.existsByNameIgnoreCaseAndIdNot(
                "repo unique workout",
                workout.getId()
        );

        boolean differentWorkoutResult = workoutRepository.existsByNameIgnoreCaseAndIdNot(
                "repo unique workout",
                -1L
        );

        assertThat(sameWorkoutResult).isFalse();
        assertThat(differentWorkoutResult).isTrue();
    }

    @Test
    void findAllByOrderByNameAsc_returnsWorkoutsSortedByName() {
        AppUser owner = persistUser("repo_sort_owner");

        persistWorkout("Repo Z Workout", "Last alphabetically", owner, false);
        persistWorkout("Repo A Workout", "First alphabetically", owner, false);

        flushAndClear();

        List<String> resultNames = workoutRepository.findAllByOrderByNameAsc()
                .stream()
                .map(Workout::getName)
                .filter(name -> name.startsWith("Repo "))
                .toList();

        assertThat(resultNames).containsSubsequence("Repo A Workout", "Repo Z Workout");
    }

    @Test
    void findVisibleByUsernameOrderByNameAsc_returnsOwnedAndSharedWorkouts() {
        AppUser marko = persistUser("repo_visible_marko");
        AppUser ana = persistUser("repo_visible_ana");

        Workout ownedWorkout = persistWorkout("Repo Marko Private", "Owned by marko", marko, false);
        Workout sharedWorkout = persistWorkout("Repo Ana Shared", "Shared by ana", ana, true);
        Workout privateOtherWorkout = persistWorkout("Repo Ana Private", "Private by ana", ana, false);

        flushAndClear();

        List<Workout> result = workoutRepository.findVisibleByUsernameOrderByNameAsc("repo_visible_marko");

        assertThat(result)
                .extracting(Workout::getName)
                .contains(ownedWorkout.getName(), sharedWorkout.getName())
                .doesNotContain(privateOtherWorkout.getName());
    }

    @Test
    void findVisibleWithDetailsByIdAndUsername_returnsOwnedWorkoutWithExercisesAndSets() {
        AppUser owner = persistUser("repo_details_owner");
        Exercise exercise = persistExercise("Repo Bench Press", ExerciseType.STRENGTH, owner, false);
        Workout workout = persistWorkoutWithExerciseAndSet("Repo Push Details", owner, false, exercise);

        flushAndClear();

        Optional<Workout> result = workoutRepository.findVisibleWithDetailsByIdAndUsername(
                workout.getId(),
                "repo_details_owner"
        );

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Repo Push Details");
        assertThat(result.get().getOwner().getUsername()).isEqualTo("repo_details_owner");
        assertThat(result.get().getWorkoutExercises()).hasSize(1);

        WorkoutExercise workoutExercise = result.get().getWorkoutExercises().iterator().next();
        assertThat(workoutExercise.getExercise().getName()).isEqualTo("Repo Bench Press");
        assertThat(workoutExercise.getSets()).hasSize(1);

        WorkoutExerciseSet set = workoutExercise.getSets().iterator().next();
        assertThat(set.getSetNumber()).isEqualTo(1);
        assertThat(set.getNumberOfReps()).isEqualTo(10);
        assertThat(set.getWeight()).isEqualTo(60.0);
    }

    @Test
    void findVisibleWithDetailsByIdAndUsername_returnsSharedWorkoutFromAnotherUser() {
        AppUser marko = persistUser("repo_shared_marko");
        AppUser ana = persistUser("repo_shared_ana");

        Workout sharedWorkout = persistWorkout("Repo Shared Workout", "Shared workout", ana, true);

        flushAndClear();

        Optional<Workout> result = workoutRepository.findVisibleWithDetailsByIdAndUsername(
                sharedWorkout.getId(),
                "repo_shared_marko"
        );

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Repo Shared Workout");
        assertThat(result.get().getOwner().getUsername()).isEqualTo("repo_shared_ana");
    }

    @Test
    void findVisibleWithDetailsByIdAndUsername_returnsEmptyForPrivateWorkoutFromAnotherUser() {
        AppUser marko = persistUser("repo_private_marko");
        AppUser ana = persistUser("repo_private_ana");

        Workout privateWorkout = persistWorkout("Repo Private Workout", "Private workout", ana, false);

        flushAndClear();

        Optional<Workout> result = workoutRepository.findVisibleWithDetailsByIdAndUsername(
                privateWorkout.getId(),
                "repo_private_marko"
        );

        assertThat(result).isEmpty();
    }

    @Test
    void findWithDetailsByIdAndOwnerUsername_returnsWorkoutOnlyForOwner() {
        AppUser marko = persistUser("repo_owner_marko");
        AppUser ana = persistUser("repo_owner_ana");

        Workout workout = persistWorkout("Repo Owner Workout", "Owned by marko", marko, false);

        flushAndClear();

        Optional<Workout> ownerResult = workoutRepository.findWithDetailsByIdAndOwnerUsername(
                workout.getId(),
                "repo_owner_marko"
        );

        Optional<Workout> otherUserResult = workoutRepository.findWithDetailsByIdAndOwnerUsername(
                workout.getId(),
                "repo_owner_ana"
        );

        assertThat(ownerResult).isPresent();
        assertThat(ownerResult.get().getName()).isEqualTo("Repo Owner Workout");
        assertThat(otherUserResult).isEmpty();
    }

    @Test
    void findWithDetailsById_returnsWorkoutWithExercisesAndSets() {
        AppUser owner = persistUser("repo_find_details_owner");
        Exercise exercise = persistExercise("Repo Deadlift", ExerciseType.STRENGTH, owner, false);
        Workout workout = persistWorkoutWithExerciseAndSet("Repo Pull Details", owner, false, exercise);

        flushAndClear();

        Optional<Workout> result = workoutRepository.findWithDetailsById(workout.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Repo Pull Details");
        assertThat(result.get().getWorkoutExercises()).hasSize(1);

        WorkoutExercise workoutExercise = result.get().getWorkoutExercises().iterator().next();
        assertThat(workoutExercise.getExercise().getName()).isEqualTo("Repo Deadlift");
        assertThat(workoutExercise.getSets()).hasSize(1);
    }

    private AppUser persistUser(String username) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("hash");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        entityManager.persist(user);
        return user;
    }

    private Exercise persistExercise(String name, ExerciseType type, AppUser owner, boolean shared) {
        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setDescription(name + " description");
        exercise.setType(type);
        exercise.setOwner(owner);
        exercise.setShared(shared);
        exercise.setMuscleGroups(new LinkedHashSet<>());

        entityManager.persist(exercise);
        return exercise;
    }

    private Workout persistWorkout(String name, String description, AppUser owner, boolean shared) {
        Workout workout = new Workout();
        workout.setName(name);
        workout.setDescription(description);
        workout.setOwner(owner);
        workout.setShared(shared);
        workout.setWorkoutExercises(new LinkedHashSet<>());

        entityManager.persist(workout);
        return workout;
    }

    private Workout persistWorkoutWithExerciseAndSet(
            String name,
            AppUser owner,
            boolean shared,
            Exercise exercise
    ) {
        Workout workout = persistWorkout(name, name + " description", owner, shared);

        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.setWorkout(workout);
        workoutExercise.setExercise(exercise);
        workoutExercise.setPosition(1);

        WorkoutExerciseSet set = new WorkoutExerciseSet();
        set.setWorkoutExercise(workoutExercise);
        set.setSetNumber(1);
        set.setNumberOfReps(10);
        set.setWeight(60.0);

        workoutExercise.setSets(new LinkedHashSet<>(Set.of(set)));
        workout.setWorkoutExercises(new LinkedHashSet<>(Set.of(workoutExercise)));

        entityManager.persist(workoutExercise);
        entityManager.persist(set);

        return workout;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
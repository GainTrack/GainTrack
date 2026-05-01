package hr.tvz.gaintrack.repository;

import hr.tvz.gaintrack.model.Workout;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {

	@EntityGraph(attributePaths = {"workoutExercises", "workoutExercises.exercise"})
	List<Workout> findAllByOrderByNameAsc();

	@EntityGraph(attributePaths = {"workoutExercises", "workoutExercises.exercise", "workoutExercises.sets"})
	Optional<Workout> findWithDetailsById(Long id);

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

	@EntityGraph(attributePaths = {"owner", "workoutExercises", "workoutExercises.exercise"})
	@Query("""
	       select distinct w
	       from Workout w
	       where w.owner.username = :username or w.shared = true
	       order by w.name asc
	       """)
	List<Workout> findVisibleByUsernameOrderByNameAsc(@Param("username") String username);

	@EntityGraph(attributePaths = {"owner", "workoutExercises", "workoutExercises.exercise", "workoutExercises.sets"})
	@Query("""
	       select distinct w
	       from Workout w
	       where w.id = :id
	         and (w.owner.username = :username or w.shared = true)
	       """)
	Optional<Workout> findVisibleWithDetailsByIdAndUsername(@Param("id") Long id,
															@Param("username") String username);

	@EntityGraph(attributePaths = {"owner", "workoutExercises", "workoutExercises.exercise", "workoutExercises.sets"})
	Optional<Workout> findWithDetailsByIdAndOwnerUsername(Long id, String username);
}

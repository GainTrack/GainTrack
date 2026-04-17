package hr.tvz.gaintrack.repository;

import hr.tvz.gaintrack.model.Workout;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {

	@EntityGraph(attributePaths = {"workoutExercises", "workoutExercises.exercise"})
	List<Workout> findAllByOrderByNameAsc();

	@EntityGraph(attributePaths = {"workoutExercises", "workoutExercises.exercise", "workoutExercises.sets"})
	Optional<Workout> findWithDetailsById(Long id);

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}

package hr.tvz.gaintrack.repository;

import hr.tvz.gaintrack.model.Exercise;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    @Override
    @EntityGraph(attributePaths = {"muscleGroups"})
    List<Exercise> findAll();

    @EntityGraph(attributePaths = {"muscleGroups"})
    List<Exercise> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = {"muscleGroups"})
    List<Exercise> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByNameAsc(
            String name,
            String description
    );
}
package hr.tvz.gaintrack.repository;

import hr.tvz.gaintrack.model.Workout;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    @Override
    @EntityGraph(attributePaths = {"workoutExercises", "workoutExercises.exercise", "workoutExercises.sets"})
    List<Workout> findAll();
}

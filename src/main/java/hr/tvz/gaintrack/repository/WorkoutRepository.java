package hr.tvz.gaintrack.repository;

import hr.tvz.gaintrack.model.Workout;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
}

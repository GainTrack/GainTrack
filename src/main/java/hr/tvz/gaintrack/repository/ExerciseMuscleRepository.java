package hr.tvz.gaintrack.repository;

import hr.tvz.gaintrack.model.ExerciseMuscle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseMuscleRepository extends JpaRepository<ExerciseMuscle, Long> {

    void deleteByExercise_Id(Long exerciseId);
}
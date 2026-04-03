package hr.tvz.gaintrack.repository;

import hr.tvz.gaintrack.model.MuscleGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MuscleGroupRepository extends JpaRepository<MuscleGroup, Long> {
}
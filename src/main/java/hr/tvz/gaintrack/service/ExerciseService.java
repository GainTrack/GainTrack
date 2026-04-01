package hr.tvz.gaintrack.service;

import hr.tvz.gaintrack.model.Exercise;
import hr.tvz.gaintrack.repository.ExerciseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;

    public ExerciseService(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    public List<Exercise> findAll() {
        return exerciseRepository.findAll();
    }

    @Transactional
    public void deleteById(Long id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found with id: " + id));

        exercise.getMuscleGroups().clear();
        exerciseRepository.save(exercise);

        exerciseRepository.delete(exercise);
    }

    public List<Exercise> search(String search) {
        if (search == null || search.trim().isEmpty()) {
            return exerciseRepository.findAllByOrderByNameAsc();
        }

        String query = search.trim();
        return exerciseRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByNameAsc(query, query);
    }
}
package hr.tvz.gaintrack.service;

import hr.tvz.gaintrack.model.Exercise;
import hr.tvz.gaintrack.repository.ExerciseMuscleRepository;
import hr.tvz.gaintrack.repository.ExerciseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseMuscleRepository exerciseMuscleRepository;

    public ExerciseService(ExerciseRepository exerciseRepository,
                           ExerciseMuscleRepository exerciseMuscleRepository) {
        this.exerciseRepository = exerciseRepository;
        this.exerciseMuscleRepository = exerciseMuscleRepository;
    }

    public List<Exercise> findAll() {
        return exerciseRepository.findAll();
    }

    @Transactional
    public void deleteById(Long id) {
        exerciseMuscleRepository.deleteByExercise_Id(id);
        exerciseRepository.deleteById(id);
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
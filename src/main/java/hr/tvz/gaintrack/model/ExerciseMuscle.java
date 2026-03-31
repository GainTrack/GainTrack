package hr.tvz.gaintrack.model;

import jakarta.persistence.*;

@Entity
@Table(name = "exercise_muscle")
public class  ExerciseMuscle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @ManyToOne(optional = false)
    @JoinColumn(name = "muscle_group_id", nullable = false)
    private MuscleGroup muscleGroup;


    public ExerciseMuscle() {
    }

    public Long getId() {
        return id;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public MuscleGroup getMuscleGroup() {
        return muscleGroup;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public void setMuscleGroup(MuscleGroup muscleGroup) {
        this.muscleGroup = muscleGroup;
    }
}

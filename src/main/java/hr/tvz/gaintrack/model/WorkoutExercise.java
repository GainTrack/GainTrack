package hr.tvz.gaintrack.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "workout_exercise",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_workout_exercise_position", columnNames = {"workout_id", "position"})
        }
)
public class WorkoutExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(nullable = false)
    private Integer position;

    @OneToMany(mappedBy = "workoutExercise", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("setOrder ASC")
    private List<WorkoutExerciseSet> sets = new ArrayList<>();

    public WorkoutExercise() {
    }

    public Long getId() {
        return id;
    }

    public Workout getWorkout() {
        return workout;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public Integer getPosition() {
        return position;
    }

    public List<WorkoutExerciseSet> getSets() {
        return sets;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setWorkout(Workout workout) {
        this.workout = workout;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public void setSets(List<WorkoutExerciseSet> sets) {
        this.sets = sets;
    }
}

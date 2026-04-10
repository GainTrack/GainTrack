package hr.tvz.gaintrack.model;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "workout_exercise",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"workout_id", "exercise_id", "position"})
        }
)
public class WorkoutExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(nullable = false)
    private Integer position;

    @OneToMany(mappedBy = "workoutExercise", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("setNumber ASC")
    private Set<WorkoutExerciseSet> sets = new LinkedHashSet<>();

    public WorkoutExercise() {
    }

    public WorkoutExercise(Long id, Workout workout, Exercise exercise, Integer position, Set<WorkoutExerciseSet> sets) {
        this.id = id;
        this.workout = workout;
        this.exercise = exercise;
        this.position = position;
        this.sets = sets;
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

    public Set<WorkoutExerciseSet> getSets() {
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

    public void setSets(Set<WorkoutExerciseSet> sets) {
        this.sets = sets;
    }
}

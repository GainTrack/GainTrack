package hr.tvz.gaintrack.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "workout_exercise_set",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_workout_exercise_set_order",
                        columnNames = {"workout_exercise_id", "set_order"}
                )
        }
)
public class WorkoutExerciseSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_exercise_id", nullable = false)
    private WorkoutExercise workoutExercise;

    @Column(name = "set_order", nullable = false)
    private Integer setOrder;

    @Column(name = "number_of_reps", nullable = false)
    private Integer numberOfReps;

    @Column(name = "weight_kg", nullable = false, precision = 6, scale = 2)
    private BigDecimal weightKg;

    public WorkoutExerciseSet() {
    }

    public Long getId() {
        return id;
    }

    public WorkoutExercise getWorkoutExercise() {
        return workoutExercise;
    }

    public Integer getSetOrder() {
        return setOrder;
    }

    public Integer getNumberOfReps() {
        return numberOfReps;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setWorkoutExercise(WorkoutExercise workoutExercise) {
        this.workoutExercise = workoutExercise;
    }

    public void setSetOrder(Integer setOrder) {
        this.setOrder = setOrder;
    }

    public void setNumberOfReps(Integer numberOfReps) {
        this.numberOfReps = numberOfReps;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }
}

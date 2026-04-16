package hr.tvz.gaintrack.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "workout_exercise_set",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"workout_exercise_id", "set_number"})
        }
)
public class WorkoutExerciseSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "workout_exercise_id", nullable = false)
    private WorkoutExercise workoutExercise;

    @Column(name = "set_number", nullable = false)
    private Integer setNumber;

    @Column(name = "number_of_reps")
    private Integer numberOfReps;

    @Column
    private Double weight;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    public WorkoutExerciseSet() {
    }

    public WorkoutExerciseSet(Long id, WorkoutExercise workoutExercise, Integer setNumber, Integer numberOfReps, Double weight, Integer durationMinutes) {
        this.id = id;
        this.workoutExercise = workoutExercise;
        this.setNumber = setNumber;
        this.numberOfReps = numberOfReps;
        this.weight = weight;
        this.durationMinutes = durationMinutes;
    }

    public Long getId() {
        return id;
    }

    public WorkoutExercise getWorkoutExercise() {
        return workoutExercise;
    }

    public Integer getSetNumber() {
        return setNumber;
    }

    public Integer getNumberOfReps() {
        return numberOfReps;
    }

    public Double getWeight() {
        return weight;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setWorkoutExercise(WorkoutExercise workoutExercise) {
        this.workoutExercise = workoutExercise;
    }

    public void setSetNumber(Integer setNumber) {
        this.setNumber = setNumber;
    }

    public void setNumberOfReps(Integer numberOfReps) {
        this.numberOfReps = numberOfReps;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
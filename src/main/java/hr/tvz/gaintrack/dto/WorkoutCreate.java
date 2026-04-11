package hr.tvz.gaintrack.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WorkoutCreate {

    @NotBlank(message = "Workout name is required.")
    private String name;

    private String description;

    @NotEmpty(message = "Add at least one exercise.")
    @Valid
    private List<WorkoutExerciseCreate> exercises = new ArrayList<>();

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<WorkoutExerciseCreate> getExercises() {
        return exercises;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExercises(List<WorkoutExerciseCreate> exercises) {
        this.exercises = exercises;
    }

    public static class WorkoutExerciseCreate {

        @NotNull(message = "Select an exercise.")
        private Long exerciseId;

        @NotEmpty(message = "Add at least one set.")
        @Valid
        private List<WorkoutExerciseSetCreate> sets = new ArrayList<>();

        public Long getExerciseId() {
            return exerciseId;
        }

        public List<WorkoutExerciseSetCreate> getSets() {
            return sets;
        }

        public void setExerciseId(Long exerciseId) {
            this.exerciseId = exerciseId;
        }

        public void setSets(List<WorkoutExerciseSetCreate> sets) {
            this.sets = sets;
        }
    }

    public static class WorkoutExerciseSetCreate {

        @NotNull(message = "Set number is required.")
        @Min(value = 1, message = "Set number must be at least 1.")
        private Integer setNumber;

        @NotNull(message = "Reps are required.")
        @Min(value = 1, message = "Reps must be at least 1.")
        private Integer numberOfReps;

        @NotNull(message = "Weight is required.")
        @DecimalMin(value = "0.0", message = "Weight cannot be negative.")
        private Double weight;

        public Integer getSetNumber() {
            return setNumber;
        }

        public Integer getNumberOfReps() {
            return numberOfReps;
        }

        public Double getWeight() {
            return weight;
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
    }
}



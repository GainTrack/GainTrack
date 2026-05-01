package hr.tvz.gaintrack.model;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "workout",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "name")
        }
)
public class Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @Column(nullable = false)
    private boolean shared = false;

    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private Set<WorkoutExercise> workoutExercises = new LinkedHashSet<>();

    public Workout() {
    }

    public Workout(Long id, String name, String description, Set<WorkoutExercise> workoutExercises) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.workoutExercises = workoutExercises;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<WorkoutExercise> getWorkoutExercises() {
        return workoutExercises;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AppUser getOwner() {
        return owner;
    }

    public boolean isShared() {
        return shared;
    }

    public void setOwner(AppUser owner) {
        this.owner = owner;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public void setWorkoutExercises(Set<WorkoutExercise> workoutExercises) {
        this.workoutExercises = workoutExercises;
    }
}

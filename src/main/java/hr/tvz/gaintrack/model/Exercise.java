package hr.tvz.gaintrack.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "exercise",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_exercise_name", columnNames = "name")
        }
)
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExerciseType type;

    @ManyToMany
    @JoinTable(
            name = "exercise_muscle_group",
            joinColumns = @JoinColumn(name = "exercise_id"),
            inverseJoinColumns = @JoinColumn(name = "muscle_group_id"),
            uniqueConstraints = {
                    @UniqueConstraint(
                            name = "uk_exercise_muscle_group",
                            columnNames = {"exercise_id", "muscle_group_id"}
                    )
            }
    )
    private Set<MuscleGroup> muscleGroups = new HashSet<>();

    public Exercise() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ExerciseType getType() { return type; }
    public Set<MuscleGroup> getMuscleGroups() { return muscleGroups; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setType(ExerciseType type) { this.type = type; }
    public void setMuscleGroups(Set<MuscleGroup> muscleGroups) { this.muscleGroups = muscleGroups; }
}

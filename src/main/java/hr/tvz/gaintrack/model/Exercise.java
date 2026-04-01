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
}

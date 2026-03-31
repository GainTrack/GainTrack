package hr.tvz.gaintrack.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "exercise")
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

    @OneToMany(mappedBy = "exercise", fetch = FetchType.LAZY)
    private Set<ExerciseMuscle> exerciseMuscles = new HashSet<>();

    public Exercise() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ExerciseType getType() { return type; }
    public Set<ExerciseMuscle> getExerciseMuscles() { return exerciseMuscles; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setType(ExerciseType type) { this.type = type; }
    public void setExerciseMuscles(Set<ExerciseMuscle> exerciseMuscles) { this.exerciseMuscles = exerciseMuscles; }
}
package hr.tvz.gaintrack.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "muscle_group",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_muscle_group_name", columnNames = "name")
        }
)
public class MuscleGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    public MuscleGroup() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package hr.tvz.gaintrack.model;

import jakarta.persistence.*;

@Entity
@Table(name = "muscle_group")
public class MuscleGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
}
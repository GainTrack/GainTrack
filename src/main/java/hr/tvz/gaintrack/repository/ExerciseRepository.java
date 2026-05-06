package hr.tvz.gaintrack.repository;

import hr.tvz.gaintrack.model.Exercise;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    @Override
    @EntityGraph(attributePaths = {"muscleGroups", "owner"})
    List<Exercise> findAll();

    @EntityGraph(attributePaths = {"muscleGroups", "owner"})
    List<Exercise> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = {"muscleGroups", "owner"})
    List<Exercise> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByNameAsc(
            String name,
            String description
    );

    @EntityGraph(attributePaths = {"muscleGroups", "owner"})
    @Query("""
           select distinct e
           from Exercise e
           where e.owner.username = :username or e.shared = true
           order by e.name asc
           """)
    List<Exercise> findVisibleByUsernameOrderByNameAsc(@Param("username") String username);

    @EntityGraph(attributePaths = {"muscleGroups", "owner"})
    @Query("""
           select distinct e
           from Exercise e
           where (e.owner.username = :username or e.shared = true)
             and (
                lower(e.name) like lower(concat('%', :search, '%'))
                or lower(e.description) like lower(concat('%', :search, '%'))
             )
           order by e.name asc
           """)
    List<Exercise> searchVisibleByUsernameOrderByNameAsc(@Param("username") String username,
                                                         @Param("search") String search);

    @EntityGraph(attributePaths = {"muscleGroups", "owner"})
    @Query("""
           select distinct e
           from Exercise e
           where e.id = :id
             and (e.owner.username = :username or e.shared = true)
           """)
    Optional<Exercise> findVisibleByIdAndUsername(@Param("id") Long id,
                                                  @Param("username") String username);

    @EntityGraph(attributePaths = {"muscleGroups", "owner"})
    Optional<Exercise> findByIdAndOwnerUsername(Long id, String username);
}
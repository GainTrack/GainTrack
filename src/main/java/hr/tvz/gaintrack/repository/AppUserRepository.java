package hr.tvz.gaintrack.repository;

import hr.tvz.gaintrack.dto.AdminUserListItem;
import hr.tvz.gaintrack.model.AppUser;
import hr.tvz.gaintrack.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    long countByRole(UserRole role);

    long countByEnabled(boolean enabled);

    @Query("""
           select new hr.tvz.gaintrack.dto.AdminUserListItem(
               u.id,
               u.username,
               u.email,
               u.role,
               u.enabled,
               u.createdAt,
               count(w.id)
           )
           from AppUser u
           left join Workout w on w.owner = u
           where (:searchPattern is null
                  or lower(u.username) like :searchPattern
                  or lower(u.email) like :searchPattern)
             and (:role is null or u.role = :role)
             and (:enabled is null or u.enabled = :enabled)
           group by u.id, u.username, u.email, u.role, u.enabled, u.createdAt
           order by lower(u.username) asc
           """)
    List<AdminUserListItem> findAdminUserListItems(@Param("searchPattern") String searchPattern,
                                                   @Param("role") UserRole role,
                                                   @Param("enabled") Boolean enabled);
}

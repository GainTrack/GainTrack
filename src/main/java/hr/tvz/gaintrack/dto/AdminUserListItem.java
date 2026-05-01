package hr.tvz.gaintrack.dto;

import hr.tvz.gaintrack.model.UserRole;

import java.time.LocalDateTime;

public record AdminUserListItem(
        Long id,
        String username,
        String email,
        UserRole role,
        boolean enabled,
        LocalDateTime createdAt,
        long workoutCount
) {
}

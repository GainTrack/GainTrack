package hr.tvz.gaintrack.dto;

public record AdminUserSummary(
        long totalUsers,
        long adminUsers,
        long enabledUsers,
        long disabledUsers
) {
}

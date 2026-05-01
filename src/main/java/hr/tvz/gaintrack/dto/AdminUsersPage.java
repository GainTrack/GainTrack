package hr.tvz.gaintrack.dto;

import java.util.List;

public record AdminUsersPage(
        List<AdminUserListItem> users,
        AdminUserSummary summary
) {
}

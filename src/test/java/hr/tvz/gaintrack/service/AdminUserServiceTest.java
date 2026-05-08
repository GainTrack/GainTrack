package hr.tvz.gaintrack.service;

import hr.tvz.gaintrack.dto.AdminUserListItem;
import hr.tvz.gaintrack.dto.AdminUsersPage;
import hr.tvz.gaintrack.model.UserRole;
import hr.tvz.gaintrack.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    void getUsersPage_buildsSummaryAndReturnsUsers() {
        AdminUserListItem user = userListItem("ana", UserRole.USER, true);
        stubSummaryCounts();
        when(appUserRepository.findAdminUserListItems(null, null, null)).thenReturn(List.of(user));

        AdminUsersPage result = adminUserService.getUsersPage(null, null, null);

        assertThat(result.users()).containsExactly(user);
        assertThat(result.summary().totalUsers()).isEqualTo(5);
        assertThat(result.summary().adminUsers()).isEqualTo(1);
        assertThat(result.summary().enabledUsers()).isEqualTo(4);
        assertThat(result.summary().disabledUsers()).isEqualTo(1);
        verify(appUserRepository).findAdminUserListItems(null, null, null);
    }

    @Test
    void getUsersPage_normalizesSearchRoleAndEnabledStatus() {
        stubSummaryCounts();
        when(appUserRepository.findAdminUserListItems("%marko%", UserRole.USER, true)).thenReturn(List.of());

        AdminUsersPage result = adminUserService.getUsersPage("  Marko  ", " user ", "enabled");

        assertThat(result.users()).isEmpty();
        verify(appUserRepository).findAdminUserListItems("%marko%", UserRole.USER, true);
    }

    @Test
    void getUsersPage_parsesDisabledStatus() {
        stubSummaryCounts();
        when(appUserRepository.findAdminUserListItems(null, null, false)).thenReturn(List.of());

        adminUserService.getUsersPage("   ", null, "disabled");

        verify(appUserRepository).findAdminUserListItems(null, null, false);
    }

    @Test
    void getUsersPage_ignoresInvalidRoleAndStatus() {
        stubSummaryCounts();
        when(appUserRepository.findAdminUserListItems("%ana%", null, null)).thenReturn(List.of());

        adminUserService.getUsersPage("Ana", "manager", "archived");

        verify(appUserRepository).findAdminUserListItems("%ana%", null, null);
    }

    private void stubSummaryCounts() {
        when(appUserRepository.count()).thenReturn(5L);
        when(appUserRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);
        when(appUserRepository.countByEnabled(true)).thenReturn(4L);
        when(appUserRepository.countByEnabled(false)).thenReturn(1L);
    }

    private static AdminUserListItem userListItem(String username, UserRole role, boolean enabled) {
        return new AdminUserListItem(
                1L,
                username,
                username + "@example.com",
                role,
                enabled,
                LocalDateTime.of(2026, 1, 1, 12, 0),
                3
        );
    }
}

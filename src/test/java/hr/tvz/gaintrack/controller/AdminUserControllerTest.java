package hr.tvz.gaintrack.controller;

import hr.tvz.gaintrack.dto.AdminUserListItem;
import hr.tvz.gaintrack.dto.AdminUserSummary;
import hr.tvz.gaintrack.dto.AdminUsersPage;
import hr.tvz.gaintrack.model.UserRole;
import hr.tvz.gaintrack.service.AdminUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private AdminUserService adminUserService;

    @InjectMocks
    private AdminUserController adminUserController;

    @Test
    void showUsers_addsUsersSummaryAndFiltersToModel() {
        AdminUserListItem user = new AdminUserListItem(
                1L,
                "marko",
                "marko@example.com",
                UserRole.USER,
                true,
                LocalDateTime.of(2026, 1, 1, 12, 0),
                2
        );
        AdminUserSummary summary = new AdminUserSummary(3, 1, 2, 1);
        when(adminUserService.getUsersPage("marko", "USER", "enabled"))
                .thenReturn(new AdminUsersPage(List.of(user), summary));

        Model model = new ExtendedModelMap();
        String view = adminUserController.showUsers("marko", "USER", "enabled", model);

        assertThat(view).isEqualTo("admin/users");
        assertThat(model.getAttribute("users")).isEqualTo(List.of(user));
        assertThat(model.getAttribute("summary")).isEqualTo(summary);
        assertThat(model.getAttribute("search")).isEqualTo("marko");
        assertThat(model.getAttribute("role")).isEqualTo("USER");
        assertThat(model.getAttribute("status")).isEqualTo("enabled");
        verify(adminUserService).getUsersPage("marko", "USER", "enabled");
    }
}

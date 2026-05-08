package hr.tvz.gaintrack.service;

import hr.tvz.gaintrack.model.AppUser;
import hr.tvz.gaintrack.model.UserRole;
import hr.tvz.gaintrack.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private AppUserDetailsService appUserDetailsService;

    @Test
    void loadUserByUsername_returnsEnabledUserDetailsWithRole() {
        AppUser appUser = appUser("admin", UserRole.ADMIN, true);
        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(appUser));

        UserDetails result = appUserDetailsService.loadUserByUsername("admin");

        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getPassword()).isEqualTo("hash");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
        verify(appUserRepository).findByUsername("admin");
    }

    @Test
    void loadUserByUsername_mapsDisabledUsers() {
        AppUser appUser = appUser("marko", UserRole.USER, false);
        when(appUserRepository.findByUsername("marko")).thenReturn(Optional.of(appUser));

        UserDetails result = appUserDetailsService.loadUserByUsername("marko");

        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_throwsWhenUserDoesNotExist() {
        when(appUserRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserDetailsService.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: missing");
    }

    private static AppUser appUser(String username, UserRole role, boolean enabled) {
        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setEmail(username + "@example.com");
        appUser.setPasswordHash("hash");
        appUser.setRole(role);
        appUser.setEnabled(enabled);
        return appUser;
    }
}

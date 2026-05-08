package hr.tvz.gaintrack.service;

import hr.tvz.gaintrack.dto.RegistrationCreate;
import hr.tvz.gaintrack.model.AppUser;
import hr.tvz.gaintrack.model.UserRole;
import hr.tvz.gaintrack.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationService registrationService;

    @Captor
    private ArgumentCaptor<AppUser> appUserCaptor;

    @Test
    void register_savesNormalizedEnabledUserWithEncodedPassword() {
        RegistrationCreate request = registrationCreate("  Marko  ", "  MARKO@EXAMPLE.COM  ", "password123");
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        registrationService.register(request);

        verify(appUserRepository).existsByUsernameIgnoreCase("Marko");
        verify(appUserRepository).existsByEmailIgnoreCase("marko@example.com");
        verify(passwordEncoder).encode("password123");
        verify(appUserRepository).save(appUserCaptor.capture());

        AppUser savedUser = appUserCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("Marko");
        assertThat(savedUser.getEmail()).isEqualTo("marko@example.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(savedUser.isEnabled()).isTrue();
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    void register_throwsWhenUsernameAlreadyExists() {
        RegistrationCreate request = registrationCreate("marko", "marko@example.com", "password123");
        when(appUserRepository.existsByUsernameIgnoreCase("marko")).thenReturn(true);

        assertThatThrownBy(() -> registrationService.register(request))
                .isInstanceOf(RegistrationException.class)
                .hasMessage("Username is already in use.")
                .extracting("field")
                .isEqualTo("username");

        verify(appUserRepository).existsByUsernameIgnoreCase("marko");
        verify(appUserRepository, never()).existsByEmailIgnoreCase("marko@example.com");
        verify(appUserRepository, never()).save(org.mockito.ArgumentMatchers.any(AppUser.class));
    }

    @Test
    void register_throwsWhenEmailAlreadyExists() {
        RegistrationCreate request = registrationCreate("marko", "marko@example.com", "password123");
        when(appUserRepository.existsByEmailIgnoreCase("marko@example.com")).thenReturn(true);

        assertThatThrownBy(() -> registrationService.register(request))
                .isInstanceOf(RegistrationException.class)
                .hasMessage("Email is already in use.")
                .extracting("field")
                .isEqualTo("email");

        verify(appUserRepository).existsByUsernameIgnoreCase("marko");
        verify(appUserRepository).existsByEmailIgnoreCase("marko@example.com");
        verify(appUserRepository, never()).save(org.mockito.ArgumentMatchers.any(AppUser.class));
    }

    private static RegistrationCreate registrationCreate(String username, String email, String password) {
        RegistrationCreate request = new RegistrationCreate();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);
        request.setConfirmPassword(password);
        return request;
    }
}

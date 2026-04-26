package hr.tvz.gaintrack.service;

import hr.tvz.gaintrack.dto.RegistrationCreate;
import hr.tvz.gaintrack.model.AppUser;
import hr.tvz.gaintrack.model.UserRole;
import hr.tvz.gaintrack.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class RegistrationService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegistrationCreate request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (appUserRepository.existsByUsernameIgnoreCase(username)) {
            throw new RegistrationException("username", "Username is already in use.");
        }

        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new RegistrationException("email", "Email is already in use.");
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setEmail(email);
        appUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        appUser.setRole(UserRole.USER);
        appUser.setEnabled(true);
        appUser.setCreatedAt(LocalDateTime.now());

        appUserRepository.save(appUser);
    }
}


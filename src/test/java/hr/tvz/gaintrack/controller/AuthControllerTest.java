package hr.tvz.gaintrack.controller;

import hr.tvz.gaintrack.dto.RegistrationCreate;
import hr.tvz.gaintrack.service.RegistrationException;
import hr.tvz.gaintrack.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private AuthController authController;

    @Test
    void showLoginPage_returnsLoginViewForAnonymousUsers() {
        String view = authController.showLoginPage(null);

        assertThat(view).isEqualTo("auth/login");
    }

    @Test
    void showLoginPage_redirectsAuthenticatedUsersHome() {
        String view = authController.showLoginPage(authentication("marko"));

        assertThat(view).isEqualTo("redirect:/");
    }

    @Test
    void showRegisterPage_addsFormObjectForAnonymousUsers() {
        Model model = new ExtendedModelMap();

        String view = authController.showRegisterPage(null, model);

        assertThat(view).isEqualTo("auth/register");
        assertThat(model.getAttribute("registrationCreate")).isInstanceOf(RegistrationCreate.class);
    }

    @Test
    void showRegisterPage_keepsExistingFormObject() {
        RegistrationCreate existingForm = registrationCreate("marko", "marko@example.com", "password123", "password123");
        Model model = new ExtendedModelMap();
        model.addAttribute("registrationCreate", existingForm);

        String view = authController.showRegisterPage(null, model);

        assertThat(view).isEqualTo("auth/register");
        assertThat(model.getAttribute("registrationCreate")).isSameAs(existingForm);
    }

    @Test
    void showRegisterPage_redirectsAuthenticatedUsersHome() {
        Model model = new ExtendedModelMap();

        String view = authController.showRegisterPage(authentication("marko"), model);

        assertThat(view).isEqualTo("redirect:/");
    }

    @Test
    void register_delegatesToServiceAndRedirectsToLogin() {
        RegistrationCreate request = registrationCreate("marko", "marko@example.com", "password123", "password123");
        BindingResult bindingResult = bindingResult(request);

        String view = authController.register(request, bindingResult, null);

        assertThat(view).isEqualTo("redirect:/login?registered");
        assertThat(bindingResult.hasErrors()).isFalse();
        verify(registrationService).register(request);
    }

    @Test
    void register_returnsRegisterViewWhenPasswordsDoNotMatch() {
        RegistrationCreate request = registrationCreate("marko", "marko@example.com", "password123", "different123");
        BindingResult bindingResult = bindingResult(request);

        String view = authController.register(request, bindingResult, null);

        assertThat(view).isEqualTo("auth/register");
        assertThat(bindingResult.hasFieldErrors("confirmPassword")).isTrue();
        verifyNoInteractions(registrationService);
    }

    @Test
    void register_returnsRegisterViewWhenBindingHasErrors() {
        RegistrationCreate request = registrationCreate("marko", "marko@example.com", "password123", "password123");
        BindingResult bindingResult = bindingResult(request);
        bindingResult.rejectValue("username", "invalid", "Invalid username.");

        String view = authController.register(request, bindingResult, null);

        assertThat(view).isEqualTo("auth/register");
        verifyNoInteractions(registrationService);
    }

    @Test
    void register_mapsRegistrationExceptionToFieldError() {
        RegistrationCreate request = registrationCreate("marko", "marko@example.com", "password123", "password123");
        BindingResult bindingResult = bindingResult(request);
        doThrow(new RegistrationException("email", "Email is already in use."))
                .when(registrationService)
                .register(request);

        String view = authController.register(request, bindingResult, null);

        assertThat(view).isEqualTo("auth/register");
        assertThat(bindingResult.hasFieldErrors("email")).isTrue();
    }

    @Test
    void register_redirectsAuthenticatedUsersHome() {
        RegistrationCreate request = registrationCreate("marko", "marko@example.com", "password123", "password123");
        BindingResult bindingResult = bindingResult(request);

        String view = authController.register(request, bindingResult, authentication("marko"));

        assertThat(view).isEqualTo("redirect:/");
        verifyNoInteractions(registrationService);
    }

    private static BindingResult bindingResult(RegistrationCreate request) {
        return new BeanPropertyBindingResult(request, "registrationCreate");
    }

    private static RegistrationCreate registrationCreate(String username, String email, String password, String confirmPassword) {
        RegistrationCreate request = new RegistrationCreate();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);
        request.setConfirmPassword(confirmPassword);
        return request;
    }

    private static Authentication authentication(String username) {
        return new UsernamePasswordAuthenticationToken(
                username,
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}

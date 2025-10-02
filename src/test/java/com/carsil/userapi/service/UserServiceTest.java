package com.carsil.userapi.service;

import com.carsil.userapi.model.User;
import com.carsil.userapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Optional;
import java.util.NoSuchElementException;
import java.lang.IllegalArgumentException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService();
        try {
            var repoField = UserService.class.getDeclaredField("userRepository");
            repoField.setAccessible(true);
            repoField.set(userService, userRepository);

            var encField = UserService.class.getDeclaredField("passwordEncoder");
            encField.setAccessible(true);
            encField.set(userService, passwordEncoder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getAll_shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(new User(), new User()));
        assertThat(userService.getAll()).hasSize(2);
        verify(userRepository).findAll();
    }

    @Test
    void create_shouldEncodePasswordAndSaveUser() {
        User u = new User();
        u.setName("luis");
        u.setEmail("luis@test.com");
        u.setPassword("raw");

        when(passwordEncoder.encode("raw")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.create(u);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User toSave = captor.getValue();

        assertThat(toSave.getPassword()).isEqualTo("ENCODED");
        assertThat(saved.getPassword()).isEqualTo("ENCODED");
        verify(passwordEncoder).encode("raw");
    }

    @Test
    void delete_shouldCallDeleteById_whenUserExists() {
        when(userRepository.existsById(10L)).thenReturn(true);
        userService.delete(10L);
        verify(userRepository).deleteById(10L);
    }

    @Test
    void validateLogin_shouldNotThrowException_onValidCredentials() {
        User u = new User();
        u.setName("luis");
        u.setPassword("HASH");

        when(userRepository.findByName("luis")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("secret", "HASH")).thenReturn(true);

        userService.validateLogin("luis", "secret");

        verify(userRepository).findByName("luis");
    }

    @Test
    void validateLogin_shouldThrowBadCredentialsException_whenUserNotFound() {
        when(userRepository.findByName("nope")).thenReturn(Optional.empty());
        assertThatThrownBy(() ->
                userService.validateLogin("nope", "x"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Usuario no valido");
    }

    @Test
    void validateLogin_shouldThrowBadCredentialsException_whenPasswordMismatch() {
        User u = new User();
        u.setName("luis");
        u.setPassword("HASH");
        when(userRepository.findByName("luis")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("bad", "HASH")).thenReturn(false);

        assertThatThrownBy(() ->
                userService.validateLogin("luis", "bad"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Contraseña incorrecta");
    }

    @Test
    void validateLogin_shouldThrowIllegalArgumentException_whenUsernameIsMissing() {
        assertThatThrownBy(() ->
                userService.validateLogin("", "secret"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuario y contraseña son obligatorios");
    }

    @Test
    void validateLogin_shouldThrowIllegalArgumentException_whenPasswordIsMissing() {
        assertThatThrownBy(() ->
                userService.validateLogin("luis", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuario y contraseña son obligatorios");
    }
}
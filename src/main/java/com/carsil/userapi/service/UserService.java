package com.carsil.userapi.service;

import com.carsil.userapi.model.User;
import com.carsil.userapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAll() {
        return userRepository.findAll();
    }


    public User create(User u) {
        Objects.requireNonNull(u, "El usuario no puede ser nulo.");
        if (u.getPassword() == null || u.getPassword().isBlank())
            throw new IllegalArgumentException("La contraseña es obligatoria y no puede estar vacía.");
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        return userRepository.save(u);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id))
            throw new NoSuchElementException("Usuario no encontrado con id: " + id);
        userRepository.deleteById(id);
    }

    public boolean validateLogin(String name, String rawPassword) {

        if (!StringUtils.hasText(name) || !StringUtils.hasText(rawPassword)) {
            throw new IllegalArgumentException("Usuario y contraseña son obligatorios");
        }
        var user = userRepository.findByName(name)
                .orElseThrow(() -> new BadCredentialsException("Usuario no valido "));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Contraseña incorrecta");
        }
        return true;
    }

    public Optional<User> getById(Long id) {
        return Optional.ofNullable(userRepository.findById(id).
                orElseThrow(() -> new NoSuchElementException(
                        "Usuario con ID " + id + " no fue encontrado.")));
    }

    public Optional<User> getByName(String name) {
        return Optional.ofNullable(userRepository.findByName(name).orElseThrow(() -> new NoSuchElementException(
                "No existe un usuario registrado con el nombre '" + name + "'.")));
    }
}


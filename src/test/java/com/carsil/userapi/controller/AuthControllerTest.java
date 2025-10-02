package com.carsil.userapi.controller;

import com.carsil.userapi.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@WithMockUser(username = "test", roles = {"USER"})
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;


    @Test
    void login_returns200_onValidCredentials() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"luis\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Login successful")));
    }

    @Test
    void login_returns401_whenBadCredentialsExceptionIsThrown() throws Exception {
        Mockito.doThrow(new BadCredentialsException("Contraseña incorrecta"))
                .when(userService).validateLogin("luis", "bad");

        mvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"luis\",\"password\":\"bad\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_returns400_whenIllegalArgumentExceptionIsThrown() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Usuario y contraseña son obligatorios"))
                .when(userService).validateLogin("", "secret");

        mvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"\",\"password\":\"secret\"}"))
                .andExpect(status().isBadRequest());
    }
}
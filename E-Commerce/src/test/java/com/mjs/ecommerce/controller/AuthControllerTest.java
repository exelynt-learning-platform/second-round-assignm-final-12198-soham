package com.mjs.ecommerce.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjs.ecommerce.dto.*;
import com.mjs.ecommerce.model.*;
import com.mjs.ecommerce.repository.UserRepository;
import com.mjs.ecommerce.security.JwtTokenProvider;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean(name = "jwtTokenProvider")
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------- LOGIN SUCCESS --------------------

//    @Test
//    void testLogin_Success() throws Exception {
//
//        LoginRequest loginRequest = new LoginRequest();
//        loginRequest.setEmail("test@mail.com");
//        loginRequest.setPassword("123456");
//
//        Mockito.when(authenticationManager.authenticate(any()))
//                .thenReturn(Mockito.mock(org.springframework.security.core.Authentication.class));
//
//        Mockito.when(tokenProvider.generateToken(any()))
//                .thenReturn("dummy-jwt-token");
//
//        mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.accessToken").value("dummy-jwt-token"));
//    }

    // -------------------- LOGIN FAILURE --------------------

    @Test
    void testLogin_Failure() throws Exception {

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@mail.com");
        loginRequest.setPassword("wrong");

        Mockito.when(authenticationManager.authenticate(any()))
                .thenThrow(new RuntimeException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------- REGISTER SUCCESS --------------------

    @Test
    void testRegister_Success() throws Exception {

        SignUpRequest request = new SignUpRequest();
        request.setName("Soham");
        request.setEmail("test@mail.com");
        request.setPassword("123456");

        Mockito.when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.empty());

        Mockito.when(passwordEncoder.encode(any()))
                .thenReturn("encoded-password");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@mail.com");
        savedUser.setName("Soham");
        savedUser.setRole(Role.USER);

        Mockito.when(userRepository.save(any()))
                .thenReturn(savedUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }

    // -------------------- REGISTER USER ALREADY EXISTS --------------------

    @Test
    void testRegister_UserAlreadyExists() throws Exception {

        SignUpRequest request = new SignUpRequest();
        request.setName("Soham");
        request.setEmail("test@mail.com");
        request.setPassword("123456");

        Mockito.when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

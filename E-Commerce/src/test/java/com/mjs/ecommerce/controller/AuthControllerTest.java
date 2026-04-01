package com.mjs.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjs.ecommerce.dto.LoginRequest;
import com.mjs.ecommerce.dto.SignUpRequest;
import com.mjs.ecommerce.model.User;
import com.mjs.ecommerce.repository.UserRepository;
import com.mjs.ecommerce.security.JwtTokenProvider;
import com.mjs.ecommerce.service.RateLimiterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.containsString;
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

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private RateLimiterService rateLimiterService;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================
    // LOGIN SUCCESS
    // =========================
    @Test
    void login_success() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("Password123");

        Authentication authentication = mock(Authentication.class);

        when(rateLimiterService.allowRequest(anyString())).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // =========================
    // LOGIN RATE LIMIT EXCEEDED
    // =========================
    @Test
    void login_rateLimitExceeded() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("Password123");

        when(rateLimiterService.allowRequest(anyString())).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests()); // or 429 if handler added
    }

    // =========================
    // LOGIN BAD CREDENTIALS
    // =========================
    @Test
    void login_badCredentials() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("wrong");

        when(rateLimiterService.allowRequest(anyString())).thenReturn(true);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================
    // REGISTER SUCCESS
    // =========================
    @Test
    void register_success() throws Exception {

        SignUpRequest request = new SignUpRequest();
        request.setName("Test User");
        request.setEmail("new@test.com");
        request.setPassword("Password123");

        when(rateLimiterService.allowRequest(anyString())).thenReturn(true);
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Test User");
        savedUser.setEmail("new@test.com");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    // =========================
    // REGISTER EMAIL EXISTS
    // =========================
    @Test
    void register_emailExists() throws Exception {

        SignUpRequest request = new SignUpRequest();
        request.setName("Test");
        request.setEmail("existing@test.com");
        request.setPassword("Password123");

        when(rateLimiterService.allowRequest(anyString())).thenReturn(true);
        when(userRepository.findByEmail("existing@test.com"))
                .thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(containsString("Email already registered")));
    }

    // =========================
    // REGISTER INVALID EMAIL
    // =========================
    @Test
    void register_invalidEmail() throws Exception {

        SignUpRequest request = new SignUpRequest();
        request.setName("Test");
        request.setEmail("invalid-email");
        request.setPassword("Password123");

        when(rateLimiterService.allowRequest(anyString())).thenReturn(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    // =========================
    // REGISTER WEAK PASSWORD
    // =========================
    @Test
    void register_weakPassword() throws Exception {

        SignUpRequest request = new SignUpRequest();
        request.setName("Test");
        request.setEmail("test@test.com");
        request.setPassword("weak");

        when(rateLimiterService.allowRequest(anyString())).thenReturn(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
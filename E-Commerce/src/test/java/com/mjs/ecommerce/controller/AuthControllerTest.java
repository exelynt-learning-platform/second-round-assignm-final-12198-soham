package com.mjs.ecommerce.controller;

import com.mjs.ecommerce.dto.JwtAuthenticationResponse;
import com.mjs.ecommerce.dto.LoginRequest;
import com.mjs.ecommerce.dto.SignUpRequest;
import com.mjs.ecommerce.model.Role;
import com.mjs.ecommerce.model.User;
import com.mjs.ecommerce.repository.UserRepository;
import com.mjs.ecommerce.security.JwtAuthenticationFilter;
import com.mjs.ecommerce.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private AuthenticationManager authenticationManager;
//
//    @MockBean
//    private UserRepository userRepository;
//
//    @MockBean
//    private PasswordEncoder passwordEncoder;
//
//    @MockBean
//    private JwtTokenProvider tokenProvider;
//
//    // ✅ LOGIN SUCCESS
//    @Test
//    void login_Success() throws Exception {
//
//        LoginRequest request = new LoginRequest();
//        request.setEmail("test@example.com");
//        request.setPassword("password");
//
//        // ✅ DO NOT mock Authentication (fix for Java 23 issue)
//        var authentication =
//                new UsernamePasswordAuthenticationToken("test@example.com", "password");
//
//        when(authenticationManager.authenticate(any()))
//                .thenReturn(authentication);
//
//        when(tokenProvider.generateToken(authentication))
//                .thenReturn("mock-token");
//
//        mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.accessToken").value("mock-token"));
//    }
//
//    // ❌ LOGIN FAILURE
//    @Test
//    void login_InvalidCredentials() throws Exception {
//
//        LoginRequest request = new LoginRequest();
//        request.setEmail("test@example.com");
//        request.setPassword("wrong");
//
//        when(authenticationManager.authenticate(any()))
//                .thenThrow(new RuntimeException("Bad credentials"));
//
//        mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isInternalServerError()); // ⚠️ unless you add exception handler
//    }
//
//    // ✅ REGISTER SUCCESS
//    @Test
//    void register_Success() throws Exception {
//
//        SignUpRequest request = new SignUpRequest();
//        request.setName("John");
//        request.setEmail("john@example.com");
//        request.setPassword("password");
//
//        when(userRepository.findByEmail("john@example.com"))
//                .thenReturn(Optional.empty());
//
//        when(passwordEncoder.encode("password"))
//                .thenReturn("encoded-password");
//
//        User savedUser = new User();
//        savedUser.setId(1L);
//        savedUser.setName("John");
//        savedUser.setEmail("john@example.com");
//        savedUser.setPassword("encoded-password");
//        savedUser.setRole(Role.USER);
//
//        when(userRepository.save(any(User.class))).thenReturn(savedUser);
//
//        mockMvc.perform(post("/api/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.email").value("john@example.com"))
//                .andExpect(jsonPath("$.role").value("USER"));
//    }
//
//    // ❌ REGISTER DUPLICATE EMAIL
//    @Test
//    void register_EmailAlreadyExists() throws Exception {
//
//        SignUpRequest request = new SignUpRequest();
//        request.setName("John");
//        request.setEmail("john@example.com");
//        request.setPassword("password");
//
//        when(userRepository.findByEmail("john@example.com"))
//                .thenReturn(Optional.of(new User()));
//
//        mockMvc.perform(post("/api/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//    }
//
//    // ❌ REGISTER INVALID INPUT
//    @Test
//    void register_InvalidInput() throws Exception {
//
//        SignUpRequest request = new SignUpRequest();
//        request.setName(""); // invalid
//        request.setEmail("invalid-email");
//        request.setPassword("");
//
//        mockMvc.perform(post("/api/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//    }
//
}

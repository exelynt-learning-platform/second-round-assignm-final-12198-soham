package com.mjs.ecommerce.config;



import com.mjs.ecommerce.security.CustomUserDetailsService;
import com.mjs.ecommerce.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private ApplicationContext context;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    // =========================
    // PASSWORD ENCODER BEAN
    // =========================
    @Test
    void passwordEncoderBean_shouldBeCreated() {
        PasswordEncoder encoder = context.getBean(PasswordEncoder.class);
        assertNotNull(encoder);

        String encoded = encoder.encode("test123");
        assertTrue(encoder.matches("test123", encoded));
    }

    // =========================
    // AUTHENTICATION MANAGER
    // =========================
    @Test
    void authenticationManagerBean_shouldBeCreated() {
        AuthenticationManager manager = context.getBean(AuthenticationManager.class);
        assertNotNull(manager);
    }

    // =========================
    // JWT FILTER BEAN
    // =========================
    @Test
    void jwtFilterBean_shouldBeCreated() {
        JwtAuthenticationFilter filter = context.getBean(JwtAuthenticationFilter.class);
        assertNotNull(filter);
    }

    // =========================
    // SECURITY FILTER CHAIN
    // =========================
    @Test
    void securityFilterChain_shouldBeCreated() {
        SecurityFilterChain chain = context.getBean(SecurityFilterChain.class);
        assertNotNull(chain);
    }


}
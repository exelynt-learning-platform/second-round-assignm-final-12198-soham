package com.mjs.ecommerce.controller;

import com.mjs.ecommerce.exception.RateLimitExceededException;
import com.mjs.ecommerce.dto.JwtAuthenticationResponse;
import com.mjs.ecommerce.dto.LoginRequest;
import com.mjs.ecommerce.dto.SignUpRequest;
import com.mjs.ecommerce.model.Role;
import com.mjs.ecommerce.model.User;
import com.mjs.ecommerce.repository.UserRepository;
import com.mjs.ecommerce.security.JwtTokenProvider;
import com.mjs.ecommerce.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Validated
@Getter
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenProvider tokenProvider;
    @Autowired
    private RateLimiterService rateLimiterService;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        String clientIp = getClientIp(request);
        String rateLimitKey = "login:" + clientIp;

        if (!rateLimiterService.allowRequest(rateLimitKey)) {
            logger.warn("Login rate limit exceeded for IP: {}", clientIp);
            throw new RateLimitExceededException(
                    "Too many login attempts. Please try again later."
            );
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            String token = tokenProvider.generateToken(authentication);
            logger.info("User successfully logged in: {}", loginRequest.getEmail());

            return ResponseEntity.ok(new JwtAuthenticationResponse(token));

        } catch (BadCredentialsException ex) {
            logger.warn("Failed login attempt for email: {} from IP: {}",
                    loginRequest.getEmail(), clientIp);
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody SignUpRequest signUpRequest,
            HttpServletRequest request) {

        String clientIp = getClientIp(request);
        String rateLimitKey = "register:" + clientIp;

        if (!rateLimiterService.allowRequest(rateLimitKey)) {
            logger.warn("Registration rate limit exceeded for IP: {}", clientIp);
            throw new RateLimitExceededException(
                    "Too many registration attempts. Please try again later."
            );
        }

        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            logger.warn("Registration attempt with existing email: {}", signUpRequest.getEmail());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Email already registered"));
        }

        try {
            if (!isValidEmail(signUpRequest.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Invalid email format"));
            }

            if (!isValidPassword(signUpRequest.getPassword())) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Password must be at least 12 characters and contain uppercase, lowercase, numbers and special characters"));
            }

            User user = new User();
            user.setName(signUpRequest.getName());
            user.setEmail(signUpRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            user.setRole(Role.USER);

            User savedUser = userRepository.save(user);
            logger.info("New user registered: {}", savedUser.getEmail());

            return ResponseEntity.status(201)
                    .body(new UserResponse(savedUser));

        } catch (Exception ex) {
            logger.error("Error during registration", ex);
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Registration failed. Please try again."));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    private boolean isValidEmail(@Email String email) {

        return email != null &&
                email.matches("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[^A-Za-z0-9].*");

        return password.length() >= 12 &&
                password.length() <= 64 &&
                hasUpper &&
                hasLower &&
                hasDigit &&
                hasSpecial;
    }

    @Getter
    public static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }

    @Getter
    public static class UserResponse {
        private final Long id;
        private final String name;
        private final String email;
        private final String role;

        public UserResponse(User user) {
            this.id = user.getId();
            this.name = user.getName();
            this.email = user.getEmail();
            this.role = user.getRole().name();
        }
    }
}
package com.mjs.ecommerce.controller;

import com.mjs.ecommerce.Exception.RateLimitExceededException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
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

    /**
     * Login endpoint with rate limiting and security hardening
     */
    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        // Extract client identifier (IP address)
        String clientIp = getClientIp(request);
        String rateLimitKey = "login:" + clientIp;

        // Check rate limit
        if (!rateLimiterService.allowRequest(rateLimitKey)) {
            logger.warn("Login rate limit exceeded for IP: {}", clientIp);
            throw new RateLimitExceededException(
                    "Too many login attempts. Please try again later."
            );
        }

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Generate JWT token
            String token = tokenProvider.generateToken(authentication);
            logger.info("User successfully logged in: {}", loginRequest.getEmail());

            return ResponseEntity.ok(new JwtAuthenticationResponse(token));

        } catch (BadCredentialsException ex) {
            // Log failed attempt but don't expose specific details
            logger.warn("Failed login attempt for email: {} from IP: {}",
                    loginRequest.getEmail(), clientIp);
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    /**
     * Register endpoint with rate limiting and validation
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody SignUpRequest signUpRequest,
            HttpServletRequest request) {

        // Extract client identifier (IP address)
        String clientIp = getClientIp(request);
        String rateLimitKey = "register:" + clientIp;

        // Check rate limit for registration (more lenient than login)
        if (!rateLimiterService.allowRequest(rateLimitKey)) {
            logger.warn("Registration rate limit exceeded for IP: {}", clientIp);
            throw new RateLimitExceededException(
                    "Too many registration attempts. Please try again later."
            );
        }

        // Check if email already exists
        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            logger.warn("Registration attempt with existing email: {}", signUpRequest.getEmail());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Email already registered"));
        }

        try {
            // Validate email format (additional validation)
            if (!isValidEmail(signUpRequest.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Invalid email format"));
            }

            // Validate password strength
            if (!isValidPassword(signUpRequest.getPassword())) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Password must be at least 8 characters and contain uppercase, lowercase, and numbers"));
            }

            // Create new user
            User user = new User();
            user.setName(signUpRequest.getName());
            user.setEmail(signUpRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            user.setRole(Role.USER);

            User savedUser = userRepository.save(user);
            logger.info("New user registered: {}", savedUser.getEmail());

            // Don't return password in response
            return ResponseEntity.status(201)
                    .body(new UserResponse(savedUser));

        } catch (Exception ex) {
            logger.error("Error during registration", ex);
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Registration failed. Please try again."));
        }
    }

    /**
     * Extract client IP address from request
     */
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

    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Validate password strength
     * Requirements: minimum 8 characters, uppercase, lowercase, numbers
     */
    private boolean isValidPassword(String password) {
        return password != null &&
                password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*");
        
    }

    /**
     * Simple error response class
     */
    public static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * User response class (doesn't expose password)
     */
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

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }
}
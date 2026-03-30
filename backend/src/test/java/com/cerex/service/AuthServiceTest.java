package com.cerex.service;

import com.cerex.domain.User;
import com.cerex.domain.User.UserRole;
import com.cerex.domain.User.UserStatus;
import com.cerex.dto.auth.AuthResponse;
import com.cerex.dto.auth.LoginRequest;
import com.cerex.dto.auth.RegisterRequest;
import com.cerex.exception.BusinessException;
import com.cerex.exception.DuplicateResourceException;
import com.cerex.repository.UserRepository;
import com.cerex.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for AuthService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
            .email("chef@cerex.com")
            .password("P@ssw0rd123!")
            .firstName("Jean")
            .lastName("Dupont")
            .acceptTerms(true)
            .acceptPrivacyPolicy(true)
            .build();

        loginRequest = new LoginRequest();
        loginRequest.setEmail("chef@cerex.com");
        loginRequest.setPassword("P@ssw0rd123!");

        testUser = User.builder()
            .id(UUID.randomUUID())
            .email("chef@cerex.com")
            .passwordHash("$2a$12$hashedpassword")
            .firstName("Jean")
            .lastName("Dupont")
            .role(UserRole.USER)
            .status(UserStatus.ACTIVE)
            .build();
    }

    @Nested
    @DisplayName("Registration")
    class RegistrationTests {

        @Test
        @DisplayName("Should register a new user successfully")
        void shouldRegisterNewUser() {
            // Given
            given(userRepository.existsByEmail("chef@cerex.com")).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("$2a$12$encoded");
            given(userRepository.save(any(User.class))).willAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(UUID.randomUUID());
                return u;
            });
            given(jwtService.generateToken(any(User.class))).willReturn("jwt-access-token");
            given(jwtService.generateRefreshToken(any(User.class))).willReturn("jwt-refresh-token");
            given(kafkaTemplate.send(anyString(), anyString(), any()))
                .willReturn(CompletableFuture.completedFuture(null));

            // When
            AuthResponse response = authService.register(registerRequest, "127.0.0.1");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("jwt-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("jwt-refresh-token");
            assertThat(response.getUser().getEmail()).isEqualTo("chef@cerex.com");

            then(userRepository).should().save(any(User.class));
            then(kafkaTemplate).should().send(eq("cerex.user.registered"), anyString(), any());
        }

        @Test
        @DisplayName("Should reject registration with existing email")
        void shouldRejectDuplicateEmail() {
            // Given
            given(userRepository.existsByEmail("chef@cerex.com")).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.register(registerRequest, "127.0.0.1"))
                .isInstanceOf(DuplicateResourceException.class);

            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Should reject registration without accepting terms")
        void shouldRejectWithoutTerms() {
            // Given
            registerRequest.setAcceptTerms(false);

            // When & Then
            assertThatThrownBy(() -> authService.register(registerRequest, "127.0.0.1"))
                .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("Login")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with correct credentials")
        void shouldLoginSuccessfully() {
            // Given
            given(userRepository.findByEmail("chef@cerex.com")).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("P@ssw0rd123!", testUser.getPasswordHash())).willReturn(true);
            given(jwtService.generateToken(testUser)).willReturn("jwt-access-token");
            given(jwtService.generateRefreshToken(testUser)).willReturn("jwt-refresh-token");

            // When
            AuthResponse response = authService.login(loginRequest, "127.0.0.1");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("jwt-access-token");
        }

        @Test
        @DisplayName("Should reject login with wrong password")
        void shouldRejectWrongPassword() {
            // Given
            given(userRepository.findByEmail("chef@cerex.com")).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("wrong-pass", testUser.getPasswordHash())).willReturn(false);

            loginRequest.setPassword("wrong-pass");

            // When & Then
            assertThatThrownBy(() -> authService.login(loginRequest, "127.0.0.1"))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Should reject login with unknown email")
        void shouldRejectUnknownEmail() {
            // Given
            given(userRepository.findByEmail("unknown@cerex.com")).willReturn(Optional.empty());

            loginRequest.setEmail("unknown@cerex.com");

            // When & Then
            assertThatThrownBy(() -> authService.login(loginRequest, "127.0.0.1"))
                .isInstanceOf(BusinessException.class);
        }
    }
}

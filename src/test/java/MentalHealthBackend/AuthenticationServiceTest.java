package MentalHealthBackend;

import com.marindulja.mentalhealthbackend.dtos.auth.JwtAuthenticationResponse;
import com.marindulja.mentalhealthbackend.dtos.auth.SignInRequest;
import com.marindulja.mentalhealthbackend.dtos.auth.SignUpRequest;
import com.marindulja.mentalhealthbackend.models.RefreshToken;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.repositories.RefreshTokenRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import com.marindulja.mentalhealthbackend.services.auth.AuthenticationServiceImpl;
import com.marindulja.mentalhealthbackend.services.auth.JwtService;
import com.marindulja.mentalhealthbackend.services.auth.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(Role.PATIENT);
        lenient().when(refreshTokenRepository.findByUser(any(User.class))).thenReturn(Optional.of(new RefreshToken()));
        lenient().when(refreshTokenService.generateRefreshToken(user)).thenReturn(new RefreshToken(1L, user, "sampleToken", LocalDateTime.now().plusDays(30)));
    }


    @Test
    public void testSignUp() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        SignUpRequest signUpRequest = new SignUpRequest("testUser", "test@example.com", "password", Role.PATIENT);
        JwtAuthenticationResponse response = authenticationService.signUp(signUpRequest);

        assertNotNull(response);
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(anyString());
    }

    @Test
    public void testSignIn() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("token");
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

        SignInRequest signInRequest = new SignInRequest("test@example.com", "password");
        JwtAuthenticationResponse response = authenticationService.signIn(signInRequest);

        assertNotNull(response);
        assertEquals("token", response.getToken());
        verify(userRepository).findByEmail(anyString());
    }
}

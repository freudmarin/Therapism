package MentalHealthBackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marindulja.mentalhealthbackend.controllers.AuthenticationController;
import com.marindulja.mentalhealthbackend.dtos.JwtAuthenticationResponse;
import com.marindulja.mentalhealthbackend.dtos.SignUpRequest;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.services.auth.AuthenticationService;
import com.marindulja.mentalhealthbackend.services.auth.JwtService;
import com.marindulja.mentalhealthbackend.services.auth.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@ContextConfiguration(classes = AuthenticationController.class)
@Import(TestSecurityConfig.class)
public class AuthenticationControllerTest {

    @MockBean
    AuthenticationService authenticationService;

    @MockBean
    RefreshTokenService refreshTokenService;

    @MockBean
    JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        JwtAuthenticationResponse response = new JwtAuthenticationResponse("newUser", Role.PATIENT, "token", "refreshToken");
        when(authenticationService.signUp(any(SignUpRequest.class))).thenReturn(response);
    }

    @Test
    public void signUpTest() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest("newUser", "newuser@example.com", "password", Role.PATIENT);
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(signUpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));

    }
}

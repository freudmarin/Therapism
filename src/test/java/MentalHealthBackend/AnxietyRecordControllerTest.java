package MentalHealthBackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marindulja.mentalhealthbackend.controllers.AnxietyRecordController;
import com.marindulja.mentalhealthbackend.dtos.AnxietyRecordReadDto;
import com.marindulja.mentalhealthbackend.dtos.AnxietyRecordWriteDto;
import com.marindulja.mentalhealthbackend.dtos.PatientProfileReadDto;
import com.marindulja.mentalhealthbackend.dtos.UserReadDto;
import com.marindulja.mentalhealthbackend.models.Gender;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.services.anxiety_records.AnxietyRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnxietyRecordController.class)
@ContextConfiguration(classes = AnxietyRecordController.class)
@Import(TestSecurityConfig.class)
class AnxietyRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnxietyRecordService anxietyRecordService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void registerAnxietyLevels_ReturnsUserProfileWithUserDto_WhenSuccessful() throws Exception {
        AnxietyRecordWriteDto anxietyRecordWriteDto = new AnxietyRecordWriteDto(10,LocalDateTime.of(2024, Month.MARCH, 19, 19, 30));
        List<AnxietyRecordReadDto> anxietyRecords = new ArrayList<>();
        anxietyRecords.add(new AnxietyRecordReadDto(1L, 10,
                LocalDateTime.of(2024, Month.MARCH, 19, 19, 30)));
        PatientProfileReadDto patientProfileReadDto = new PatientProfileReadDto(new UserReadDto(1L, "user", "user@example.com", Role.PATIENT),
                1L, "+355684448934", Gender.MALE, anxietyRecords, new ArrayList<>());
        when(anxietyRecordService.registerAnxietyLevels(any(AnxietyRecordWriteDto.class))).thenReturn(patientProfileReadDto);

        mockMvc.perform(post("/api/v1/anxiety-records/current-user/register-anxiety")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(anxietyRecordWriteDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileId").value(1L))
                .andExpect(jsonPath("$.phoneNumber").value("+355684448934"))
                // Assert the size of the anxietyRecords list
                .andExpect(jsonPath("$.anxietyRecords", hasSize(1)))
                // Assert specific fields within the first item of the anxietyRecords list
                .andExpect(jsonPath("$.anxietyRecords[0].id").value(1L))
                .andExpect(jsonPath("$.anxietyRecords[0].anxietyLevel").value(10))
                .andExpect(jsonPath("$.anxietyRecords[0].recordDate").value("2024-03-19T19:30:00"));
    }

    @Test
    @WithMockUser(username="patientUser", roles={"PATIENT"})
    public void getAllRecordsOfCurrentUser_ShouldReturnRecords_WhenAuthenticatedAsPatient() throws Exception {
        // Given
        List<AnxietyRecordReadDto> anxietyRecords = Arrays.asList(
                new AnxietyRecordReadDto(1L, 5, LocalDateTime.of(2024, 3, 19, 15, 0)),
                new AnxietyRecordReadDto(2L, 7, LocalDateTime.of(2024, 3, 20, 15, 0))
        );

        given(anxietyRecordService.getAllOfCurrentUser()).willReturn(anxietyRecords);

        // When & Then
        mockMvc.perform(get("/api/v1/anxiety-records/current-user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(anxietyRecords.size()))
                .andExpect(jsonPath("$.[0].id").value(anxietyRecords.get(0).getId()))
                .andExpect(jsonPath("$.[0].anxietyLevel").value(anxietyRecords.get(0).getAnxietyLevel()))
                .andExpect(jsonPath("$.[1].id").value(anxietyRecords.get(1).getId()));
    }
}

package MentalHealthBackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marindulja.mentalhealthbackend.controllers.AnxietyRecordController;
import com.marindulja.mentalhealthbackend.dtos.anxietyrecord.AnxietyRecordReadDto;
import com.marindulja.mentalhealthbackend.dtos.anxietyrecord.AnxietyRecordWriteDto;
import com.marindulja.mentalhealthbackend.services.anxiety_records.AnxietyRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
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

    @MockBean
    private ChatClient chatClient;

    @MockBean
    private ChatClient.Builder chatClientBuilder;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testRegisterAnxietyLevels() throws Exception {
        // Given
        AnxietyRecordWriteDto anxietyRecordWriteDto = new AnxietyRecordWriteDto(10,LocalDateTime.of(2024, Month.MARCH, 19, 19, 30));
        // Set properties of anxietyRecordDto here...

        Mockito.when(chatClientBuilder.build()).thenReturn(chatClient);

        // When & Then
        mockMvc.perform(post("/api/v1/anxiety-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(anxietyRecordWriteDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRecordsOfCurrentUser_ShouldReturnRecords_WhenAuthenticatedAsPatient() throws Exception {
        // Given
        List<AnxietyRecordReadDto> anxietyRecords = Arrays.asList(
                new AnxietyRecordReadDto(1L, 5, LocalDateTime.of(2024, 3, 19, 15, 0)),
                new AnxietyRecordReadDto(2L, 7, LocalDateTime.of(2024, 3, 20, 15, 0))
        );

        given(anxietyRecordService.getAllOfCurrentPatient()).willReturn(anxietyRecords);

        // When & Then
        mockMvc.perform(get("/api/v1/anxiety-records/current-patient")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(anxietyRecords.size()))
                .andExpect(jsonPath("$.[0].id").value(anxietyRecords.get(0).getId()))
                .andExpect(jsonPath("$.[0].anxietyLevel").value(anxietyRecords.get(0).getAnxietyLevel()))
                .andExpect(jsonPath("$.[1].id").value(anxietyRecords.get(1).getId()));
    }
}

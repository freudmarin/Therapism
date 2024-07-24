package MentalHealthBackend;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.anxietyrecord.AnxietyRecordWriteDto;
import com.marindulja.mentalhealthbackend.models.*;
import com.marindulja.mentalhealthbackend.services.anxiety_records.AnxietyRecordServiceImpl;
import com.marindulja.mentalhealthbackend.services.profiles.ProfileService;
import com.marindulja.mentalhealthbackend.services.profiles.TherapistProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnxietyRecordServiceTest {

    private ProfileService profileService;

    @Mock
    private AnxietyRecordServiceImpl anxietyRecordService;

    private User currentUser;
    private PatientProfile patientProfile;


    @BeforeEach
    void setUp() {
        // Initialize profileService with a mock or actual implementation if necessary
        profileService = Mockito.mock(TherapistProfileServiceImpl.class);

        currentUser = new User(1L, "user", "test", "user@example.com",
                null, null, false, Role.PATIENT);
        patientProfile = new PatientProfile(1L, "+355684448934", Gender.MALE, false, currentUser,
                new ArrayList<>() ,new ArrayList<>(), List.of(new AnxietyRecord(1L, patientProfile, LocalDateTime.now(), 10)));
    }

    @Test
    void createAnxietyRecord_Success() {
        // Arrange
        AnxietyRecordWriteDto recordDto = new AnxietyRecordWriteDto(10, LocalDateTime.of(2024, Month.MARCH, 19, 19, 30));
        // Mocking Utilities.getCurrentUser()
        try (MockedStatic<Utilities> utilitiesMockedStatic = Mockito.mockStatic(Utilities.class)) {
            // Mocking the current user behavior
            utilitiesMockedStatic.when(Utilities::getCurrentUser).thenReturn(java.util.Optional.of(currentUser));
            // Act
            anxietyRecordService.registerAnxietyLevels(recordDto);
            verify(anxietyRecordService, times(1)).registerAnxietyLevels(recordDto);
            // Verify other properties as needed
        }
    }
}

package MentalHealthBackend;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.AnxietyRecordReadDto;
import com.marindulja.mentalhealthbackend.dtos.AnxietyRecordWriteDto;
import com.marindulja.mentalhealthbackend.dtos.PatientProfileReadDto;
import com.marindulja.mentalhealthbackend.dtos.UserReadDto;
import com.marindulja.mentalhealthbackend.dtos.mapping.ModelMappingUtility;
import com.marindulja.mentalhealthbackend.models.*;
import com.marindulja.mentalhealthbackend.repositories.AnxietyRecordRepository;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import com.marindulja.mentalhealthbackend.services.anxiety_records.AnxietyRecordServiceImpl;
import com.marindulja.mentalhealthbackend.services.profiles.ProfileService;
import com.marindulja.mentalhealthbackend.services.profiles.ProfileServiceImpl;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AnxietyRecordServiceTest {

    @Mock
    private AnxietyRecordRepository anxietyRecordRepository;

    @Mock
    private ProfileRepository userProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMappingUtility modelMapper;

    private ProfileService profileService;

    private AnxietyRecordServiceImpl anxietyRecordService;

    private User currentUser;
    private PatientProfile patientProfile;


    @BeforeEach
    void setUp() {
        // Initialize profileService with a mock or actual implementation if necessary
        profileService = Mockito.mock(ProfileServiceImpl.class);

        // Manually create the instance of AnxietyRecordServiceImpl
        anxietyRecordService = new AnxietyRecordServiceImpl(anxietyRecordRepository, userRepository, userProfileRepository, profileService, modelMapper);

        currentUser = new User(1L, "user", "test", "user@example.com",
                null, null, false, Role.PATIENT);
        patientProfile = new PatientProfile(1L, "+355684448934", Gender.MALE, false, currentUser,
                new ArrayList<>(), new ArrayList<>(),
        new ArrayList<>());
    }


    @Test
    void createAnxietyRecord_Success() {
        // Arrange
        AnxietyRecordWriteDto recordDto = new AnxietyRecordWriteDto(10, LocalDateTime.of(2024, Month.MARCH, 19, 19, 30));
        AnxietyRecord record = new AnxietyRecord(1L, patientProfile, LocalDateTime.of(2024, Month.MARCH, 19, 19, 30), 10);

        PatientProfileReadDto patientProfileReadDto = new PatientProfileReadDto(new UserReadDto(1L, "user", "user@example.com", Role.PATIENT),
                1L, "+355684448934", Gender.MALE, List.of(new AnxietyRecordReadDto(1L, 10,
                LocalDateTime.of(2024, Month.MARCH, 19, 19, 30))), new ArrayList<>());
        // Mocking Utilities.getCurrentUser()
        try (MockedStatic<Utilities> utilitiesMockedStatic = Mockito.mockStatic(Utilities.class)) {
            // Mocking the current user behavior
            utilitiesMockedStatic.when(Utilities::getCurrentUser).thenReturn(java.util.Optional.of(currentUser));

            when(userProfileRepository.findByUserId(any(Long.class))).thenReturn(java.util.Optional.of(patientProfile));
            when(modelMapper.map(recordDto, AnxietyRecord.class)).thenReturn(record);
            when(anxietyRecordRepository.save(any(AnxietyRecord.class))).thenReturn(record);
            when(profileService.findByUserId(any(Long.class))).thenReturn(patientProfileReadDto);

            // Act
            PatientProfileReadDto result = anxietyRecordService.registerAnxietyLevels(recordDto);
            // Assert
            assertEquals(patientProfileReadDto.getAnxietyRecords().get(0).getAnxietyLevel(), result.getAnxietyRecords().get(0).getAnxietyLevel());
            assertEquals(patientProfileReadDto.getAnxietyRecords().get(0).getRecordDate(), result.getAnxietyRecords().get(0).getRecordDate());
            assertEquals(patientProfileReadDto.getProfileId(), result.getProfileId());
            assertEquals(patientProfileReadDto.getPhoneNumber(), result.getPhoneNumber());
            assertEquals(patientProfileReadDto.getGender(), result.getGender());
            // Verify other properties as needed
        }
    }

    @Test
    void getAllOfCurrentUser_Success() {
        // Setup
        LocalDateTime recordDate = LocalDateTime.now();
        AnxietyRecord anxietyRecord = new AnxietyRecord(1L, patientProfile, recordDate, 10);
        patientProfile.setAnxietyRecords(Arrays.asList(anxietyRecord)); // Ensure the user profile contains the anxiety records

        AnxietyRecordReadDto expectedDto = new AnxietyRecordReadDto(anxietyRecord.getId(), anxietyRecord.getAnxietyLevel(), anxietyRecord.getRecordDate());
        List<AnxietyRecordReadDto> expectedDtos = Collections.singletonList(expectedDto);

        try (MockedStatic<Utilities> utilities = Mockito.mockStatic(Utilities.class)) {
            utilities.when(Utilities::getCurrentUser).thenReturn(java.util.Optional.of(currentUser));
            when(userProfileRepository.findByUserId(currentUser.getId())).thenReturn(java.util.Optional.of(patientProfile));

            // This mock setup handles any AnxietyRecord and maps it to an AnxietyRecordReadDto.
            when(modelMapper.map(any(AnxietyRecord.class), eq(AnxietyRecordReadDto.class))).thenReturn(expectedDto);

            // Execute
            List<AnxietyRecordReadDto> resultDtos = anxietyRecordService.getAllOfCurrentUser();

            // Verify
            assertEquals(expectedDtos.size(), resultDtos.size(), "The size of returned DTO list should match the expected size.");
            assertTrue(resultDtos.containsAll(expectedDtos), "The returned DTO list should contain all expected DTOs.");
        }
    }
    // Similarly, implement tests for retrieve, update, and delete operations
}

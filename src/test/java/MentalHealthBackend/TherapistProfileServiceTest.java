package MentalHealthBackend;


import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.mapping.DTOMappings;
import com.marindulja.mentalhealthbackend.dtos.profile.TherapistProfileReadDto;
import com.marindulja.mentalhealthbackend.dtos.profile.TherapistProfileWriteDto;
import com.marindulja.mentalhealthbackend.dtos.profile.UserProfileWriteDto;
import com.marindulja.mentalhealthbackend.dtos.specialization.SpecializationDto;
import com.marindulja.mentalhealthbackend.dtos.user.UserReadDto;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.*;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.SpecializationRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import com.marindulja.mentalhealthbackend.services.profiles.TherapistProfileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TherapistProfileServiceTest {

    @Mock
    private ProfileRepository userProfileRepository;

    @Mock

    private SpecializationRepository specializationRepository;

    @Mock
    private DTOMappings modelMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TherapistProfileServiceImpl profileService;

    @Test
    void createProfile_authorizedUser_Success() {
        // Arrange
        Long userId = 1L;
        TherapistProfileWriteDto therapistProfileWriteDto = new TherapistProfileWriteDto("+355684448934", Gender.MALE, 3, "Bsc. in Psychology", List.of(1L));
        User currentUser = new User(1L, "user", "test",
                "admin@example.com", null, null, false, Role.THERAPIST);
        TherapistProfile mockedUserProfile = new TherapistProfile(5, "Bsc. in Psychology", new ArrayList<>());
        UserReadDto userReadDto = new UserReadDto(1L, "user", "admin@example.com", Role.THERAPIST);
        // Mocking Utilities.getCurrentUser()
        try (MockedStatic<Utilities> utilitiesMockedStatic = Mockito.mockStatic(Utilities.class)) {
            // Mocking the current user behavior
            utilitiesMockedStatic.when(Utilities::getCurrentUser).thenReturn(java.util.Optional.of(currentUser));
            when(userRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(currentUser));
            // Mocking UserProfileRepository save method
            when(userRepository.findById(anyLong())).thenReturn(Optional.of(currentUser));
            when(modelMapper.toTherapistProfile(any(TherapistProfileWriteDto.class))).thenReturn(mockedUserProfile);
            when(modelMapper.toUserDTO(any(User.class))).thenReturn(userReadDto);
            when(specializationRepository.findAllById(anyList())).thenReturn(List.of(new Specialization(1L, "Specialization 1", new ArrayList<>())));
            when(userProfileRepository.save(any(TherapistProfile.class))).thenReturn(mockedUserProfile);
            when(modelMapper.toSpecializationDto(any(Specialization.class))).thenReturn(new SpecializationDto(1L, "Specialization 1"));
            // Act + Assert
            TherapistProfileReadDto therapistProfileReadDto = (TherapistProfileReadDto) profileService.createProfile(userId, therapistProfileWriteDto);
            assertEquals(therapistProfileWriteDto.getGender(), therapistProfileReadDto.getGender());
            assertEquals(therapistProfileWriteDto.getPhoneNumber(), therapistProfileReadDto.getPhoneNumber());
            assertEquals(therapistProfileWriteDto.getQualifications(), therapistProfileReadDto.getQualifications());
            assertEquals(therapistProfileWriteDto.getPhoneNumber(), therapistProfileReadDto.getPhoneNumber());
            assertEquals(therapistProfileWriteDto.getSpecializationIds(), therapistProfileReadDto.getSpecializations().stream().map(SpecializationDto::getId).toList());
        }
    }

    @Test
    void createProfile_unauthorizedUser_ThrowsUnauthorizedException() {
        // Arrange
        Long userId = 2L; // Assume a different user id
        UserProfileWriteDto userProfileCreationDto = new UserProfileWriteDto("+355684448934", Gender.MALE);
        User currentUser = new User(1L, "user", "test",
                "admin@example.com", null, null, false, Role.THERAPIST);

        try (MockedStatic<Utilities> utilitiesMockedStatic = Mockito.mockStatic(Utilities.class)) {
            // Mocking the current user behavior
            utilitiesMockedStatic.when(Utilities::getCurrentUser).thenReturn(java.util.Optional.of(currentUser));
            // Act and Assert
            assertThrows(UnauthorizedException.class, () -> profileService.createProfile(userId, userProfileCreationDto));
        }
    }
}

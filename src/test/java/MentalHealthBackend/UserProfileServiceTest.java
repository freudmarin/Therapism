package MentalHealthBackend;


import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.UserProfileReadDto;
import com.marindulja.mentalhealthbackend.dtos.UserProfileWriteDto;
import com.marindulja.mentalhealthbackend.dtos.UserReadDto;
import com.marindulja.mentalhealthbackend.dtos.mapping.ModelMappingUtility;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.Gender;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.models.UserProfile;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import com.marindulja.mentalhealthbackend.services.profiles.ProfileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private ProfileRepository userProfileRepository;

    @Mock
    private ModelMappingUtility modelMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileServiceImpl profileService;


    @Test
    void createProfile_authorizedUser_Success() {
        // Arrange
        Long userId = 1L;
        UserProfileReadDto userProfileDto = new UserProfileReadDto( 1L, "+355684448934", Gender.MALE,  new ArrayList<>(), new ArrayList<>());
        UserProfileWriteDto userProfileCreationDto = new UserProfileWriteDto( "+355684448934", Gender.MALE);
        UserProfile mockedUserProfile = new UserProfile(1L, "+355684448934",  Gender.MALE, new ArrayList<>(), new ArrayList<>());

        User currentUser = new User(1L, "user", "test",
                "admin@example.com",  null, null, false, Role.ADMIN);

        // Mocking Utilities.getCurrentUser()
        try (MockedStatic<Utilities> utilitiesMockedStatic = Mockito.mockStatic(Utilities.class)) {
            // Mocking the current user behavior
            utilitiesMockedStatic.when(Utilities::getCurrentUser).thenReturn(java.util.Optional.of(currentUser));
            when(userRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(currentUser));
            // Mocking UserProfileRepository save method
            when(userProfileRepository.save(any(UserProfile.class))).thenReturn(mockedUserProfile);
            when(modelMapper.map(userProfileCreationDto, UserProfile.class)).thenReturn(mockedUserProfile);
            when(modelMapper.map(mockedUserProfile, UserProfileReadDto.class)).thenReturn(userProfileDto);
            // Act + Assert
            UserProfileReadDto userProfileDTOSaved = profileService.createProfile(userId, userProfileCreationDto);
            assertEquals(userProfileDTOSaved.getProfileId(), userProfileDto.getProfileId());
            assertEquals(userProfileDTOSaved.getGender(), userProfileDto.getGender());
            assertEquals(userProfileDTOSaved.getPhoneNumber(), userProfileDto.getPhoneNumber());

        }
    }

    @Test
    void createProfile_unauthorizedUser_ThrowsUnauthorizedException() {
        // Arrange
        Long userId = 2L; // Assume a different user id
        UserProfileWriteDto userProfileCreationDto = new UserProfileWriteDto( "+355684448934", Gender.MALE);
        User currentUser = new User(1L, "user", "test",
                "admin@example.com", null, null, false, Role.ADMIN);

        try (MockedStatic<Utilities> utilitiesMockedStatic = Mockito.mockStatic(Utilities.class)) {
            // Mocking the current user behavior
            utilitiesMockedStatic.when(Utilities::getCurrentUser).thenReturn(java.util.Optional.of(currentUser));
            // Act and Assert
            assertThrows(UnauthorizedException.class, () -> profileService.createProfile(userId, userProfileCreationDto));
        }
    }
}

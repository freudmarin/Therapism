package MentalHealthBackend;


import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.UserProfileReadDto;
import com.marindulja.mentalhealthbackend.dtos.UserProfileWriteDto;
import com.marindulja.mentalhealthbackend.dtos.UserReadDto;
import com.marindulja.mentalhealthbackend.dtos.mapping.ModelMappingUtility;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.AdminProfile;
import com.marindulja.mentalhealthbackend.models.Gender;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.User;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
        UserProfileReadDto userProfileDto = new UserProfileReadDto( new UserReadDto(1L, "test", "admin@example.com", Role.ADMIN), 1L, "+355684448934", Gender.MALE);
        UserProfileWriteDto userProfileCreationDto = new UserProfileWriteDto( "+355684448934", Gender.MALE);
        User currentUser = new User(1L, "user", "test",
                "admin@example.com",  null, null, false, Role.ADMIN);
        AdminProfile mockedUserProfile = new AdminProfile(1L, "+355684448934",  Gender.MALE, currentUser);
        UserReadDto userReadDto = new UserReadDto(1L, "user", "admin@example.com", Role.ADMIN);
        // Mocking Utilities.getCurrentUser()
        try (MockedStatic<Utilities> utilitiesMockedStatic = Mockito.mockStatic(Utilities.class)) {
            // Mocking the current user behavior
            utilitiesMockedStatic.when(Utilities::getCurrentUser).thenReturn(java.util.Optional.of(currentUser));
            when(userRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(currentUser));
            // Mocking UserProfileRepository save method
            when(userRepository.findById(anyLong())).thenReturn(Optional.of(currentUser));
            when(userProfileRepository.save(any(AdminProfile.class))).thenReturn(mockedUserProfile);
            when(modelMapper.map(any(UserProfileWriteDto.class), eq(AdminProfile.class))).thenReturn(mockedUserProfile);
            when(modelMapper.map(any(User.class), eq(UserReadDto.class))).thenReturn(userReadDto);
            // Act + Assert
            UserProfileReadDto userProfileDTOSaved = profileService.createProfile(userId, userProfileCreationDto);


            assertEquals(userProfileDto.getProfileId(), userProfileDTOSaved.getProfileId());
            assertEquals(userProfileDto.getGender(), userProfileDTOSaved.getGender());
            assertEquals(userProfileDto.getPhoneNumber(), userProfileDTOSaved.getPhoneNumber());

        }
    }

    @Test
    void createProfile_unauthorizedUser_ThrowsUnauthorizedException() {
        // Arrange
        Long userId = 2L; // Assume a different user id
        UserProfileWriteDto userProfileCreationDto = new UserProfileWriteDto("+355684448934", Gender.MALE);
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

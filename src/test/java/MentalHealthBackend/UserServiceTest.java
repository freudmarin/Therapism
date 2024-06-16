package MentalHealthBackend;

import com.marindulja.mentalhealthbackend.dtos.mapping.ModelMappingUtility;
import com.marindulja.mentalhealthbackend.dtos.user.UserReadDto;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import com.marindulja.mentalhealthbackend.services.users.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMappingUtility modelMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void findById_UserExists_ReturnsUserDto() {
        // Arrange
        Long userId = 1L;
        UserReadDto userDto = new UserReadDto(1L, "admin", "admin@example.com", Role.ADMIN);

        User mockUser = new User(1L, "admin", "test",
                "admin@example.com", null, null, false, Role.ADMIN);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(modelMapper.map(mockUser, UserReadDto.class)).thenReturn(userDto);
        // Act
        UserReadDto result = userService.findById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userDto.getUsername(), result.getUsername());
        assertEquals(userDto.getRole(), result.getRole());
        assertEquals(userDto.getEmail(), result.getEmail());
        // Optionally, you can verify that the repository's findById method was called
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findById_UserDoesNotExist_ThrowsEntityNotFoundException() {
        // Arrange
        Long nonExistingUserId = 2L;
        when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(EntityNotFoundException.class, () -> userService.findById(nonExistingUserId));

        // Optionally, you can verify that the repository's findById method was called
        verify(userRepository, times(1)).findById(nonExistingUserId);
    }
}

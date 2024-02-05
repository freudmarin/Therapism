package com.marindulja.mentalhealthbackend.services.users;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.UserDto;
import com.marindulja.mentalhealthbackend.exceptions.InvalidInputException;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.models.UserProfile;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import com.marindulja.mentalhealthbackend.repositories.specifications.UserSpecification;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.marindulja.mentalhealthbackend.models.Role.THERAPIST;


@Service
public class UserServiceImpl implements UserService {

    private final ModelMapper mapper = new ModelMapper();
    private final UserRepository userRepository;

    private final ProfileRepository profileRepository;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, ProfileRepository profileRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public void assignPatientsToTherapist(List<Long> userIds, Long therapistId) {
        User currentUser = Utilities.getCurrentUser().get();
        User therapist = userRepository.findById(therapistId).orElseThrow(() -> new EntityNotFoundException("Therapist with id " + therapistId + "not found"));
        List<User> patients = userRepository.findAllById(userIds);
        if (currentUser.getRole() == Role.ADMIN) {
            assignTherapistToPatients(therapist, patients);
        } else if (currentUser.getRole() == THERAPIST) {
            assignTherapistToPatients(therapist, patients);
        } else
            throw new UnauthorizedException("The user with id" + currentUser.getId() + "doesn't have the required permission to perform this operation");

    }

    @Transactional
    public void assignTherapistToPatients(User therapist, List<User> patients) {
        patients.forEach(patient -> patient.setTherapist(therapist));
        userRepository.saveAll(patients);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) throws InvalidInputException {
        User currentUser = this.findByEmail(Utilities.getCurrentUser().get().getEmail());
        if (!currentUser.getId().equals(id)) {
            throw new InvalidInputException("The user can update only his profile");
        }

        if (StringUtils.isBlank(userDto.getUsername())) {
            throw new InvalidInputException("Username cannot be null or empty");
        }
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(userDto.getUsername());
                    user.setPassword(passwordEncoder.encode(userDto.getPassword()));
                    user.setUsername(userDto.getUsername());
                    User updatedUser = userRepository.save(user);
                    return mapToDTO(updatedUser);
                })
                .orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));
    }

    @Override
    public UserDto findById(Long id) {
        return userRepository.findById(id).
                map(this::mapToDTO).orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new EntityNotFoundException(String.format("User with the email %s does not exist!", email))
                );
    }

    @Override
    public void deleteById(Long id) {
        User currentUser = this.findByEmail(Utilities.getCurrentUser().get().getEmail());
        User userToBeDeleted = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id "
                        + id + " not found"));


        if (currentUser.getRole() == Role.SUPERADMIN && userToBeDeleted.getRole() == Role.ADMIN)
            deleteUserById(id);

        else if (currentUser.getRole() == Role.ADMIN && (userToBeDeleted.getRole() == THERAPIST
                || userToBeDeleted.getRole() == Role.PATIENT))
            deleteUserById(id);
        else
            throw new UnauthorizedException("User with id " + currentUser.getId() + "is not authorized to delete user with id" + id);
    }

    @Transactional
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id"
                        + id + " not found"));
        user.setDeleted(true);
        userRepository.save(user);
        UserProfile userProfile = profileRepository.findByUserId(id).orElseThrow(() -> new EntityNotFoundException("Profile for user "
                + id + " not found"));
        userProfile.setDeleted(true);
        profileRepository.save(userProfile);
    }

    @Override
    public List<UserDto> findAllByRoleFilteredAndSorted(String searchValue) {
        User currentUser = this.findByEmail(Utilities.getCurrentUser().get().getEmail());
        Specification<User> spec = (root, query, cb) -> cb.conjunction();
        if (currentUser.getRole() == Role.SUPERADMIN) {
            spec = spec.and(new UserSpecification(Arrays.asList(Role.SUPERADMIN, Role.ADMIN, Role.PATIENT,Role.THERAPIST), searchValue));
        }
        // admin can view only therapists and patients of the institution he belongs to
        if (currentUser.getRole() == Role.ADMIN) {
            spec = spec.and(new UserSpecification(Arrays.asList(Role.ADMIN, Role.PATIENT,Role.THERAPIST), searchValue));
        }

        // therapist can belong to an  institution or no
        if (currentUser.getRole() == THERAPIST) {
            spec = spec.and(new UserSpecification(List.of(Role.PATIENT), searchValue));
        }

        List<User> userListResult = userRepository.findAll(spec);
        if (currentUser.getRole() == THERAPIST) {
            userListResult.stream().filter(u -> u.getTherapist() == currentUser).collect(Collectors.toList());
        }
        return userListResult
                .stream()
                .map(this::mapToDTOForUserList)
                .collect(Collectors.toList());
    }

    private UserDto mapToDTO(User user) {
        return mapper.map(user, UserDto.class);
    }

    private UserDto mapToDTOForUserList(User user) {
        return new UserDto(user.getId(), user.getUsername(),
                user.getPassword(), user.getEmail());
    }

    private User mapToEntity(UserDto userDto) {
        return mapper.map(userDto, User.class);
    }
}

package com.marindulja.mentalhealthbackend.services.users;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.mapping.DTOMappings;
import com.marindulja.mentalhealthbackend.dtos.user.UserReadDto;
import com.marindulja.mentalhealthbackend.dtos.user.UserWriteDto;
import com.marindulja.mentalhealthbackend.exceptions.InvalidInputException;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import com.marindulja.mentalhealthbackend.repositories.specifications.UserSpecification;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static com.marindulja.mentalhealthbackend.models.Role.THERAPIST;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final DTOMappings mapper;
    private final UserRepository userRepository;

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserReadDto update(Long id, UserWriteDto userDto) throws InvalidInputException {
        final var currentUser = getCurrentUserOrThrow();
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
                    return mapper.toUserDTO(updatedUser);
                })
                .orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));
    }

    @Override
    public UserReadDto findById(Long id) {
        return userRepository.findById(id).
                map(mapper::toUserDTO).orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new EntityNotFoundException(String.format("User with the email %s does not exist!", email))
                );
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        final var currentUser = getCurrentUserOrThrow();
        final var userToBeDeleted = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));

        // Check authorization before proceeding with deletion
        if (canDeleteUser(currentUser, userToBeDeleted)) {
            userToBeDeleted.setDeleted(true);
            userRepository.save(userToBeDeleted);
            final var userProfile = profileRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Profile for user with id " + userToBeDeleted.getId() + " not found"));
            userProfile.setDeleted(true);
            profileRepository.save(userProfile);
        } else {
            throw new UnauthorizedException("User with id " + currentUser.getId() + " is not authorized to delete user with id " + id);
        }
    }

    private boolean canDeleteUser(User currentUser, User userToBeDeleted) {
        return (currentUser.getRole() == Role.SUPERADMIN && userToBeDeleted.getRole() == Role.ADMIN) ||
                (currentUser.getRole() == Role.ADMIN && (userToBeDeleted.getRole() == Role.THERAPIST || userToBeDeleted.getRole() == Role.PATIENT))
                || (currentUser.getId().equals(userToBeDeleted.getId()) && currentUser.getRole() == userToBeDeleted.getRole());
    }

    @Override
    public List<UserReadDto> findAllByRoleFilteredAndSorted(String searchValue) {
        final var currentUser = getCurrentUserOrThrow();
        Specification<User> spec = (root, query, cb) -> cb.conjunction();
        if (currentUser.getRole() == Role.SUPERADMIN) {
            spec = spec.and(new UserSpecification(Arrays.asList(Role.SUPERADMIN, Role.ADMIN, Role.PATIENT, Role.THERAPIST), searchValue));
        }
        // admin can view only therapists and patients of the institution he belongs to
        else if (currentUser.getRole() == Role.ADMIN) {
            spec = spec.and(new UserSpecification(Arrays.asList(Role.ADMIN, Role.PATIENT, Role.THERAPIST), searchValue));
        } else if (currentUser.getRole() == THERAPIST) {
            spec = spec.and(new UserSpecification(List.of(Role.ADMIN, Role.PATIENT, Role.THERAPIST), searchValue));
        } else {
            // currentUser.getRole() = patient
            spec = spec.and(new UserSpecification(List.of(THERAPIST), searchValue));
        }
        var userListResult = userRepository.findAll(spec);
        if (currentUser.getRole() == THERAPIST) {
            userListResult = userListResult.stream().filter(u -> {
                if (u.getRole() == Role.PATIENT) {
                    return u.getTherapist() == currentUser;
                }
                return true;
            }).toList();
        }
        return userListResult
                .stream()
                .map(user -> new UserReadDto(user.getId(), user.getActualUsername(), user.getEmail(), user.getRole()))
                .toList();
    }

    @Override
    public void chooseTherapist(Long therapistId) {
        final var currentUser = getCurrentUserOrThrow();
        final var user = userRepository.findById(therapistId).orElseThrow(() -> new EntityNotFoundException("Therapist with id " + therapistId + "not found"));
        if (user.getRole() != Role.THERAPIST)
            throw new InvalidInputException("User with id " + therapistId + " is not a therapist");
        else if (currentUser.getRole() != Role.PATIENT)
            throw new InvalidInputException("User with id " + currentUser.getId() + " is not a patient");
        else {
            currentUser.setTherapist(user);
            userRepository.save(currentUser);
        }
    }

    private User getCurrentUserOrThrow() {
        return Utilities.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
    }
}

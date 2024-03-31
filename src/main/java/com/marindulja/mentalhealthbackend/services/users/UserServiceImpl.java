package com.marindulja.mentalhealthbackend.services.users;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.UserReadDto;
import com.marindulja.mentalhealthbackend.dtos.UserWriteDto;
import com.marindulja.mentalhealthbackend.dtos.mapping.ModelMappingUtility;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.marindulja.mentalhealthbackend.models.Role.THERAPIST;


@Service
public class UserServiceImpl implements UserService {

    private final ModelMappingUtility mapper;
    private final UserRepository userRepository;

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(ModelMappingUtility mapper, UserRepository userRepository, ProfileRepository profileRepository, PasswordEncoder passwordEncoder) {
        this.mapper = mapper;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void assignPatientsToTherapist(List<Long> userIds, Long therapistId) {
        final var currentUser = Utilities.getCurrentUser().get();
        final var therapist = userRepository.findById(therapistId).orElseThrow(() -> new EntityNotFoundException("Therapist with id " + therapistId + "not found"));
        final var patients = userRepository.findAllById(userIds);
        if (currentUser.getRole() == Role.ADMIN) {
            assignTherapistToPatients(therapist, patients);
        } else if (currentUser.getRole() == THERAPIST && currentUser.getId().equals(therapistId)) {
            assignTherapistToPatients(therapist, patients);
        } else
            throw new UnauthorizedException("The user with id " + currentUser.getId() + "doesn't have the required permission to perform this operation");

    }

    public void assignTherapistToPatients(User therapist, List<User> patients) {
        patients.forEach(patient -> patient.setTherapist(therapist));
        userRepository.saveAll(patients);
    }

    @Override
    public UserReadDto update(Long id, UserWriteDto userDto) throws InvalidInputException {
        final var currentUser = this.findByEmail(Utilities.getCurrentUser().get().getEmail());
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
                    return mapper.map(updatedUser, UserReadDto.class);
                })
                .orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));
    }

    @Override
    public UserReadDto findById(Long id) {
        return userRepository.findById(id).
                map(user -> mapper.map(user, UserReadDto.class)).orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));
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
        final var currentUser = Utilities.getCurrentUser().get();
        final var userToBeDeleted = userRepository.findById(id)
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

    private void deleteUserById(Long id) {
        final var user = userRepository.findById(id)
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
    public List<UserReadDto> findAllByRoleFilteredAndSorted(String searchValue) {
        final var currentUser = Utilities.getCurrentUser().get();
        Specification<User> spec = (root, query, cb) -> cb.conjunction();
        if (currentUser.getRole() == Role.SUPERADMIN) {
            spec = spec.and(new UserSpecification(Arrays.asList(Role.SUPERADMIN, Role.ADMIN, Role.PATIENT, Role.THERAPIST), searchValue));
        }
        // admin can view only therapists and patients of the institution he belongs to
        else if (currentUser.getRole() == Role.ADMIN) {
            spec = spec.and(new UserSpecification(Arrays.asList(Role.ADMIN, Role.PATIENT, Role.THERAPIST), searchValue));
        } else if (currentUser.getRole() == THERAPIST) {
            spec = spec.and(new UserSpecification(List.of(Role.PATIENT, THERAPIST), searchValue));
        } else {
            // currentUser.getRole() = patient
            spec = spec.and(new UserSpecification(List.of(THERAPIST), searchValue));
        }
        final var userListResult = userRepository.findAll(spec);
        if (currentUser.getRole() == THERAPIST) {
            userListResult.stream().filter(u -> u.getTherapist() == currentUser).collect(Collectors.toList());
        }
        return userListResult
                .stream()
                .map(user -> new UserReadDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole()))
                .collect(Collectors.toList());
    }
}

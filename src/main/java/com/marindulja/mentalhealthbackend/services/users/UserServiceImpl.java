package com.marindulja.mentalhealthbackend.services.users;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.UserDto;
import com.marindulja.mentalhealthbackend.eventlisteners.UserCreatedEvent;
import com.marindulja.mentalhealthbackend.exceptions.InvalidInputException;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.Institution;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.models.UserProfile;
import com.marindulja.mentalhealthbackend.repositories.InstitutionRepository;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import com.marindulja.mentalhealthbackend.repositories.specifications.UserSpecification;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.marindulja.mentalhealthbackend.models.Role.THERAPIST;


@Service
public class UserServiceImpl implements UserService {

    private final ModelMapper mapper = new ModelMapper();
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;

    private final ProfileRepository profileRepository;

    private final PasswordEncoder passwordEncoder;

    private final ApplicationEventPublisher eventPublisher;

    public UserServiceImpl(UserRepository userRepository,
                           InstitutionRepository institutionRepository, ProfileRepository profileRepository, PasswordEncoder passwordEncoder, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.institutionRepository = institutionRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UserDto save(UserDto userDto, Role role, Long institutionId) throws InvalidInputException {
        if (StringUtils.isBlank(userDto.getUsername())) {
            throw new InvalidInputException("User name cannot be null or empty");
        }
        // set the institution id of the logged admin
        // if the user is superAdmin it will be set to null, because the superAdmin belongs to no institution
        // if ROLE == Therapist it means a new therapist is being created by an institution admin
        //so set the institutionId
        User user = mapToEntity(userDto);
        Institution institution;
        if ((role == THERAPIST || role == Role.PATIENT) && institutionId == null) {
            institution = this.findByEmail(Utilities.getCurrentUser().get().getEmail()).getInstitution();
        } else {
            institution = institutionRepository.findById(institutionId).orElseThrow(()
                    -> new EntityNotFoundException("The institution with id" + institutionId));
        }
        user.setInstitution(institution);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        eventPublisher.publishEvent(new UserCreatedEvent(user));
        return mapToDTO(savedUser);
    }

    @Override
    public void assignPatientsToTherapist(List<Long> userIds, Long therapistId) {
        User currentUser = this.findByEmail(Utilities.getCurrentUser().get().getEmail());
        User therapist = userRepository.findById(therapistId).orElseThrow(() -> new EntityNotFoundException("Therapist with id " + therapistId + "not found"));
        List<User> patients = userRepository.findAllById(userIds);
        if (currentUser.getRole() == Role.ADMIN) {
            if (therapist.getInstitution() == null) {
                throw new InvalidInputException("Therapist with id " + therapistId +  "belongs to no institution");
            }
            if (!therapist.getInstitution().getId().equals(currentUser.getInstitution().getId())) {
                throw new UnauthorizedException("Therapist with id" + therapistId + " doesn't belong to the institution " + currentUser.getInstitution().getId() +
                "with admin" + currentUser.getId());
            }

            assignTherapistToPatients(therapist, patients);
        }
        
        else if (currentUser.getRole() == THERAPIST) {
            assignTherapistToPatients(therapist, patients);
        }

        else
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
        if(!currentUser.getId().equals(id)) {
            throw new InvalidInputException("The user can update only his profile");
        }

        if (StringUtils.isBlank(userDto.getUsername())) {
            throw new InvalidInputException("Institution name cannot be null or empty");
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
    public List<UserDto> findAllByRoleFilteredAndSorted(Role role, String searchValue) {
        User currentUser = this.findByEmail(Utilities.getCurrentUser().get().getEmail());
        Specification<User> spec = (root, query, cb) -> cb.conjunction();
        if (currentUser.getRole() == Role.SUPERADMIN && role != Role.SUPERADMIN) {
            spec = spec.and(new UserSpecification(role, searchValue, null, null));
        }
        // admin can view only therapists and patients of the institution he belongs to
        if (currentUser.getRole() == Role.ADMIN && (role == THERAPIST || role == Role.PATIENT)) {
            spec = spec.and(new UserSpecification(role, searchValue, currentUser.getInstitution(), null));
        }

        // therapist can belong to an  institution or no
        if (currentUser.getRole() == THERAPIST) {
            if (role == Role.PATIENT) {
                if (currentUser.getInstitution() != null) {
                    spec = spec.and(new UserSpecification(role, searchValue, currentUser.getInstitution(), currentUser.getTherapist()));
                } else {
                    spec = spec.and(new UserSpecification(role, searchValue, null, currentUser.getTherapist()));
                }
            } else if (role == THERAPIST) {
                spec = spec.and(new UserSpecification(role, searchValue, null, null));
            }
        }
        //each patient can view all therapsists 
        if (currentUser.getRole() == Role.PATIENT && role == THERAPIST) {
           spec = spec.and(new UserSpecification(role, searchValue,  null, null)); 
        }
        
        List<User> userListResult = userRepository.findAll(spec);

        return userListResult
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private UserDto mapToDTO(User user) {
        return mapper.map(user, UserDto.class);
    }

    private User mapToEntity(UserDto userDto) {
        return mapper.map(userDto, User.class);
    }
}

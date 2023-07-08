package com.marindulja.mentalhealthbackend.services.users;

import com.marindulja.mentalhealthbackend.dtos.UserDto;
import com.marindulja.mentalhealthbackend.exceptions.InvalidInputException;
import com.marindulja.mentalhealthbackend.models.Institution;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.repositories.InstitutionRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import com.marindulja.mentalhealthbackend.repositories.specifications.UserSpecification;
import com.marindulja.mentalhealthbackend.services.auth.AuthService;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final ModelMapper mapper = new ModelMapper();
    private final UserRepository userRepository;

    private final AuthService authService;
    private final InstitutionRepository institutionRepository;


    public UserServiceImpl(UserRepository userRepository, AuthService authService,
                          InstitutionRepository institutionRepository) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.institutionRepository = institutionRepository;
    }

    @Override
    public UserDto save(UserDto userDto, Role role, Long institutionId) throws InvalidInputException {
        if (StringUtils.isBlank(userDto.getUsername())) {
            throw new InvalidInputException("Institution name cannot be null or empty");
        }
        // set the institution id of the logged admin
        // if the user is superAdmin it will be set to null, because the superAdmin belongs to no institution
        // if ROLE == Therapist it means a new therapist is being created by an institution admin
        //so set the institutionId
        User user = mapToEntity(userDto);
        Institution institution;
        if (role == Role.THERAPIST && institutionId == null) {
            institution = this.findByUsername(authService.getCurrentUser().get().getUsername()).getInstitution();
        } else {
            institution = institutionRepository.findById(institutionId).orElseThrow(()
                    -> new EntityNotFoundException("The institution with id" + institutionId));
        }
        user.setInstitution(institution);
        user.setRole(role);

        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) throws InvalidInputException {
        if (StringUtils.isBlank(userDto.getUsername())) {
            throw new InvalidInputException("Institution name cannot be null or empty");
        }
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(userDto.getUsername());
                    user.setPassword(userDto.getPassword());
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
    public User findByUsername(String username) throws EntityNotFoundException {

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new EntityNotFoundException(String.format("User with the username %s does not exist!", username))
                );
    }


    @Override
    public void deleteById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id"
                        + id + "not found"));
        user.setDeleted(true);
        userRepository.save(user);
    }

    @Override
    public List<UserDto> findAllByRoleFilteredAndSorted(Role role, String searchValue) {
        User currentUser = this.findByUsername(authService.getCurrentUser().get().getUsername());
        Specification<User> spec = (root, query, cb) -> cb.conjunction();
        if (currentUser.getRole() == Role.SUPERADMIN && role != Role.SUPERADMIN) {
            spec = spec.and(new UserSpecification(role, searchValue, null, null));
        }
        // admin can view only therapists and patients of the institution he belongs to
        if (currentUser.getRole() == Role.ADMIN && (role == Role.THERAPIST || role == Role.PATIENT)) {
            spec = spec.and(new UserSpecification(role, searchValue, currentUser.getInstitution(), null));
        }

        // therapist can belong to an  institution or no
        if (currentUser.getRole() == Role.THERAPIST && role == Role.PATIENT) {
            if (currentUser.getInstitution() != null) {
                spec = spec.and(new UserSpecification(role, searchValue, currentUser.getInstitution(), currentUser.getTherapist()));
            } else {
                spec = spec.and(new UserSpecification(role, searchValue, null, currentUser.getTherapist()));
            }
        }

        List<User> userListResult = userRepository.findAll(spec);

        return userListResult
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findAllByInstitution(Institution institution) {
        return userRepository.findAllByInstitution(institution);
    }

    private UserDto mapToDTO(User user) {
        return mapper.map(user, UserDto.class);
    }

    private User mapToEntity(UserDto userDto) {
        return mapper.map(userDto, User.class);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}

package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.UserDto;
import com.marindulja.mentalhealthbackend.dtos.UserProfileDto;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.UserProfile;
import com.marindulja.mentalhealthbackend.services.profiles.ProfileService;
import com.marindulja.mentalhealthbackend.services.users.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/users")
public class UserController {
    private final UserService userService;

    private final ProfileService userProfileService;

    public UserController(UserService userService, ProfileService userProfileService) {
        this.userService = userService;
        this.userProfileService = userProfileService;
    }

    @PostMapping("therapists/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createInstitutionTherapist(@RequestBody UserDto userDto) {
        UserDto savedTherapist = userService.save(userDto, Role.THERAPIST, null);
        return new ResponseEntity<>(savedTherapist, HttpStatus.CREATED);
    }

    @PostMapping("patients/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createInstitutionPatient(@RequestBody UserDto userDto) {
        UserDto savedPatient = userService.save(userDto, Role.PATIENT, null);
        return new ResponseEntity<>(savedPatient, HttpStatus.CREATED);
    }

    @PostMapping("therapists/{therapistId}/assignPatients")
    @PreAuthorize("hasAnyRole('ADMIN', 'THERAPIST')")
    public ResponseEntity<?> assignPatientsToTherapist(@RequestBody List<Long> userIds, @PathVariable("therapistId") Long therapistId) {
        userService.assignPatientsToTherapist(userIds, therapistId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("institution-admins/{institutionId}/add")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> createInstitutionAdmin(@RequestBody UserDto userDto, @PathVariable("institutionId") Long institutionId) {
        UserDto savedUser = userService.save(userDto, Role.ADMIN, institutionId);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }
    @PutMapping("users/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable("userId") Long userId, @RequestBody UserDto userDto) {
        UserDto savedUser = userService.update(userId, userDto);
        return new ResponseEntity<>(savedUser, HttpStatus.OK);
    }

    @GetMapping("users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','THERAPIST')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        UserDto user = userService.findById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN','THERAPIST')")
    public ResponseEntity<List<UserDto>> getAllFilteredUsersByRole(
            @RequestParam(name = "searchValue", required = false) String searchValue, @RequestParam(name = "role") String role) {
        return new ResponseEntity<>(userService.findAllByRoleFilteredAndSorted(Role.fromString(role), searchValue), HttpStatus.OK);
    }


    @GetMapping("/{id}/profile")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','THERAPIST','PATIENT')")
    public ResponseEntity<UserProfile> getUserProfileById(@PathVariable Long id) {
        UserProfile profile = userProfileService.findByUserId(id);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @PutMapping("{id}/profile")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','THERAPIST','PATIENT')")
    public ResponseEntity<?> updateUserProfile(@PathVariable Long id, @RequestBody UserProfileDto updatedProfile) {
        UserProfile profile = userProfileService.updateProfile(id, updatedProfile);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @DeleteMapping("{id}/profile")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
         userService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

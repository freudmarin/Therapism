package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.*;
import com.marindulja.mentalhealthbackend.services.profiles.ProfileService;
import com.marindulja.mentalhealthbackend.services.users.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/users")
@Slf4j
public class UserController {
    private final UserService userService;

    private final ProfileService userProfileService;

    public UserController(UserService userService, ProfileService userProfileService) {
        this.userService = userService;
        this.userProfileService = userProfileService;
    }

    @PostMapping("therapists/{therapistId}/assignPatients")
    @PreAuthorize("hasAnyRole('ADMIN', 'THERAPIST')")
    public ResponseEntity<?> assignPatientsToTherapist(@RequestBody List<Long> userIds, @PathVariable("therapistId") Long therapistId) {
        userService.assignPatientsToTherapist(userIds, therapistId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN','THERAPIST', 'PATIENT')")
    public ResponseEntity<?> updateUser(@PathVariable("userId") Long userId, @RequestBody UserWriteDto userDto) {
        final var savedUser = userService.update(userId, userDto);
        return new ResponseEntity<>(savedUser, HttpStatus.OK);
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','THERAPIST', 'PATIENT')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        final var user = userService.findById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("all")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','THERAPIST')")
    public ResponseEntity<List<UserReadDto>> getAllFilteredUsersByRole(
            @RequestParam(name = "searchValue", required = false) String searchValue) {
        return new ResponseEntity<>(userService.findAllByRoleFilteredAndSorted(searchValue), HttpStatus.OK);
    }


    @GetMapping("/{id}/profile")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','THERAPIST','PATIENT')")
    public ResponseEntity<UserProfileWithUserDto> getUserProfileById(@PathVariable Long id) {
        final var profile = userProfileService.findByUserId(id);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @PutMapping("{id}/profile")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','THERAPIST','PATIENT')")
    public ResponseEntity<UserProfileReadDto> updateUserProfile(@PathVariable Long id, @RequestBody UserProfileWriteDto updatedProfile) {
        final var profile = userProfileService.updateProfile(id, updatedProfile);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @PostMapping("{id}/profile")
    @PreAuthorize("hasAnyRole('ADMIN','THERAPIST','PATIENT')")
    public ResponseEntity<UserProfileReadDto> createUserProfile(@PathVariable Long id, @RequestBody UserProfileWriteDto userProfileCreationDto) {
        final var profile = userProfileService.createProfile(id, userProfileCreationDto);
        return new ResponseEntity<>(profile, HttpStatus.CREATED);
    }

    @DeleteMapping("{id}/profile")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

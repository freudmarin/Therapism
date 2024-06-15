package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.*;
import com.marindulja.mentalhealthbackend.services.profiles.ProfileService;
import com.marindulja.mentalhealthbackend.services.users.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Qualifier("patientProfileServiceImpl")
    private final ProfileService patientProfileService;

    @Qualifier("therapistProfileServiceImpl")
    private final ProfileService therapistProfileService;

    public UserController(UserService userService,
                          @Qualifier("patientProfileServiceImpl") ProfileService patientProfileService,
                          @Qualifier("therapistProfileServiceImpl") ProfileService therapistProfileService) {
        this.userService = userService;
        this.patientProfileService = patientProfileService;
        this.therapistProfileService = therapistProfileService;
    }

    @PostMapping("therapists/{therapistId}/assignPatients")
    @PreAuthorize("hasAnyRole('ADMIN', 'THERAPIST')")
    public ResponseEntity<?> assignPatientsToTherapist(@RequestBody List<Long> patientIds, @PathVariable("therapistId") Long therapistId) {
        userService.assignPatientsToTherapist(patientIds, therapistId);
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

    @DeleteMapping("{id}/profile")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping("{id}/therapist-profile")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','THERAPIST', 'PATIENT')")
    public ResponseEntity<UserProfileReadDto> getTherapistProfileById(@PathVariable Long id) {
        final var profile = therapistProfileService.findByUserId(id);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @GetMapping("{id}/patient-profile")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','THERAPIST', 'PATIENT')")
    public ResponseEntity<UserProfileReadDto> getPatientProfileById(@PathVariable Long id) {
        final var profile = patientProfileService.findByUserId(id);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @PostMapping("{patientId}/patient-profile")
    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    public ResponseEntity<UserProfileReadDto> createPatientProfile(@PathVariable Long patientId,
                                                                   @RequestBody PatientProfileWriteDto patientProfileDto) {
        final var profile = patientProfileService.createProfile(patientId, patientProfileDto);
        return new ResponseEntity<>(profile, HttpStatus.CREATED);
    }

    @PostMapping("{therapistId}/therapist-profile")
    @PreAuthorize("hasAnyRole('THERAPIST','PATIENT')")
    public ResponseEntity<UserProfileReadDto> createTherapistProfile(@PathVariable Long therapistId,
                                                                     @RequestBody TherapistProfileWriteDto therapistProfileDto) {
        final var profile = therapistProfileService.createProfile(therapistId, therapistProfileDto);
        return new ResponseEntity<>(profile, HttpStatus.CREATED);
    }

    @PutMapping("patient-profile/{patientId}")
    public ResponseEntity<?> updatePatientProfile(@PathVariable Long patientId,
                                                  @RequestBody PatientProfileWriteDto patientProfileDto) {
        patientProfileService.updateProfile(patientId, patientProfileDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("therapist-profile/{therapistId}")
    public ResponseEntity<?> updateTherapistProfile(@PathVariable Long therapistId,
                                                    @RequestBody TherapistProfileWriteDto therapistProfileDto) {
        therapistProfileService.updateProfile(therapistId, therapistProfileDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

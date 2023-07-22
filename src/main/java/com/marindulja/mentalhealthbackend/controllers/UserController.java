package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.UserDto;
import com.marindulja.mentalhealthbackend.models.Role;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("therapists/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTherapist(@RequestBody UserDto userDto) {
        UserDto savedTherapist = userService.save(userDto, Role.THERAPIST, null);
        return new ResponseEntity<>(savedTherapist, HttpStatus.CREATED);
    }

    @PostMapping("institution-admins/{institutionId}/add")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> createInstitutionAdmin(@RequestBody UserDto userDto, @PathVariable("institutionId") Long institutionId) {
        UserDto savedUser = userService.save(userDto, Role.ADMIN, institutionId);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @DeleteMapping("institution-admins/{adminId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> deleteInstitutionAdmin(@PathVariable("adminId") Long adminId) {
        userService.deleteById(adminId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
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
}

package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.UserDto;
import com.marindulja.mentalhealthbackend.exceptions.InvalidInputException;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.services.users.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/users")
public class  UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("therapists")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTherapist(@RequestBody UserDto userDto) {
        try {
            UserDto savedTherapist = userService.save(userDto, Role.THERAPIST, null);
            return new ResponseEntity<>(savedTherapist, HttpStatus.CREATED);
        } catch (InvalidInputException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("institution-admins/{institutionId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> createInstitutionAdmin(@RequestBody UserDto userDto, @PathVariable("institutionId") Long institutionId) {
        try {
            UserDto savedUser = userService.save(userDto, Role.ADMIN, institutionId);
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
        } catch (InvalidInputException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("institution-admins/{adminId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> deleteInstitutionAdmin(@PathVariable("adminId") Long adminId) {
        try {
            userService.deleteById(adminId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("users/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable("userId") Long userId, @RequestBody UserDto userDto) {
        try {
            UserDto savedUser = userService.update(userId, userDto);
            return new ResponseEntity<>(savedUser, HttpStatus.OK);
        } catch (InvalidInputException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','THERAPIST')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            UserDto user = userService.findById(id);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("admin/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN','THERAPIST')")
    public ResponseEntity<List<UserDto>> getAllPaginatedAndFilteredUsersByRole (
            @RequestParam(name = "searchValue") String searchValue, @RequestParam(name = "role") Role role) {
        return new ResponseEntity<>(userService.findAllByRoleFilteredAndSorted(role, searchValue), HttpStatus.OK);
    }
}

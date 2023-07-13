package com.marindulja.mentalhealthbackend.services.users;

import com.marindulja.mentalhealthbackend.dtos.UserDto;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    UserDto update(Long id, UserDto userDto);

    UserDto findById(Long id);

    User findByUsername(String username);

    void deleteById(Long id);
    List<UserDto> findAllByRoleFilteredAndSorted(Role role, String searchValue);

    UserDto save(UserDto userDto, Role role, Long institutionId);

    UserDetailsService userDetailsService();
}

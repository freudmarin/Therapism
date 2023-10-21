package com.marindulja.mentalhealthbackend.services.users;

import com.marindulja.mentalhealthbackend.dtos.UserDto;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.User;

import java.util.List;

public interface UserService  {

    UserDto update(Long id, UserDto userDto);

    UserDto findById(Long id);

    User findByEmail(String email);

    void deleteById(Long id);

    List<UserDto> findAllByRoleFilteredAndSorted(Role role, String searchValue);

    void assignPatientsToTherapist(List<Long> userIds, Long therapistId);
}

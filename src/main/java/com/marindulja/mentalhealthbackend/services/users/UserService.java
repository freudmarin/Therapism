package com.marindulja.mentalhealthbackend.services.users;

import com.marindulja.mentalhealthbackend.dtos.user.UserReadDto;
import com.marindulja.mentalhealthbackend.dtos.user.UserWriteDto;
import com.marindulja.mentalhealthbackend.models.User;

import java.util.List;

public interface UserService {

    UserReadDto update(Long id, UserWriteDto userDto);

    UserReadDto findById(Long id);

    User findByEmail(String email);

    void deleteById(Long id);

    List<UserReadDto> findAllByRoleFilteredAndSorted(String searchValue);

    void assignPatientsToTherapist(List<Long> patientIds, Long therapistId);
}

package com.marindulja.mentalhealthbackend.services.disorders;

import com.marindulja.mentalhealthbackend.dtos.DisorderDto;

import java.util.List;

public interface DisorderService {

    List<DisorderDto> getAllDisorders();

    void deleteDisorder(Long id);

    void assignDisordersToUser(Long userId, List<Long> disorderIds);
    void removeDisordersFromPatient(Long patientId, List<Long> disorderIds);
}

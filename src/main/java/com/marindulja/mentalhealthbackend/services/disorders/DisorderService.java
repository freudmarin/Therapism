package com.marindulja.mentalhealthbackend.services.disorders;

import com.marindulja.mentalhealthbackend.dtos.disorder.DisorderDto;
import com.marindulja.mentalhealthbackend.dtos.disorder.MostCommonDisordersDto;

import java.util.Collection;
import java.util.List;

public interface DisorderService {

    List<DisorderDto> getAllDisorders();

    void assignDisordersToUser(Long userId, List<Long> disorderIds);

    void updateDisordersToUser(Long patientId, Collection<Long> disorderIds);

    void removeDisordersFromPatient(Long patientId, List<Long> disorderIds);

    List<MostCommonDisordersDto> findCommonDisordersAmongHighAnxietyPatients();
}

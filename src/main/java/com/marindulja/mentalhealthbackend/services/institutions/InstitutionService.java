package com.marindulja.mentalhealthbackend.services.institutions;

import com.marindulja.mentalhealthbackend.dtos.InstitutionDto;
import com.marindulja.mentalhealthbackend.exceptions.InvalidInputException;
import com.marindulja.mentalhealthbackend.models.SubscriptionStatus;

import java.util.List;

public interface InstitutionService {

    InstitutionDto save(InstitutionDto institutionDto);

    InstitutionDto update(Long id, InstitutionDto institutionDto);

    InstitutionDto findById(Long id);

    void deleteById(Long id);
    void changeSubscriptionStatus(Long id, SubscriptionStatus newStatus);
    List<InstitutionDto> getFilteredAndSorted(String searchValue);
    boolean isInstitutionNameUnique(String name);
}

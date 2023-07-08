package com.marindulja.mentalhealthbackend.services.institutions;

import com.marindulja.mentalhealthbackend.dtos.InstitutionDto;
import com.marindulja.mentalhealthbackend.exceptions.InvalidInputException;
import com.marindulja.mentalhealthbackend.models.Institution;
import com.marindulja.mentalhealthbackend.models.SubscriptionStatus;
import com.marindulja.mentalhealthbackend.repositories.InstitutionRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import com.marindulja.mentalhealthbackend.repositories.specifications.InstitutionSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionRepository institutionRepository;

    private final UserRepository userRepository;

    private final ModelMapper mapper = new ModelMapper();


    public InstitutionServiceImpl(InstitutionRepository institutionRepository, UserRepository userRepository) {
        this.institutionRepository = institutionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public InstitutionDto save(InstitutionDto institutionDto) {
        if (institutionDto.getName() == null || institutionDto.getName().isEmpty()) {
            throw new InvalidInputException("Institution name cannot be null or empty");
        }
        Institution institution = mapToEntity(institutionDto);
        Institution savedInstitution = institutionRepository.save(institution);
        return mapToDTO(savedInstitution);
}

    @Override
    public InstitutionDto update(Long id, InstitutionDto institutionDto) {
        if (institutionDto.getName() == null || institutionDto.getName().isEmpty()) {
            throw new InvalidInputException("Institution name cannot be null or empty");
        }

        return institutionRepository.findById(id).map(institution -> {
            institution.setName(institutionDto.getName());
            institution.setAddress(institutionDto.getAddress());
            institution.setContactNumber(institutionDto.getContactNumber());
            Institution updatedInstitution = institutionRepository.save(institution);
            return mapToDTO(updatedInstitution);
        }).orElseThrow(() -> new EntityNotFoundException("Institution with id " + id + " not found"));
    }


    @Override
    public InstitutionDto findById(Long id) {
        return institutionRepository.findById(id).map(this::mapToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Institution with id" + id + "not found"));
    }

    @Override
    public List<InstitutionDto> getFilteredAndSorted(String searchValue) {
        Specification<Institution> spec = new InstitutionSpecification(searchValue);
        return institutionRepository.findAll(spec).stream().map(this::mapToDTO).toList();
    }

    @Override
    public void deleteById(Long id) {
        Institution institution = institutionRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Institution with id" + id + "not found"));
        institution.setDeleted(true);

        // delete also all the users that belong to this institution
        userRepository.findAllByInstitution(institution).forEach(user -> user.setDeleted(true));

    }

    @Override
    public void changeSubscriptionStatus(Long id, SubscriptionStatus newStatus) {
        Institution institution = institutionRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Institution with id " + id + " not found"));

        institution.setSubscriptionStatus(newStatus);

        if (newStatus.equals(SubscriptionStatus.ACTIVE)) {
            // Set expiry date 3 months ahead for active status
            LocalDateTime expiryDate = LocalDateTime.now().plusMonths(3);
            institution.setSubscriptionExpiryDate(expiryDate);
        } else {
            institution.setSubscriptionExpiryDate(null);
        }

        institutionRepository.save(institution);
    }

    @Override
    public boolean isInstitutionNameUnique(String name) {
        return institutionRepository.findByName(name).isEmpty();
    }

    private InstitutionDto mapToDTO(Institution institution) {
        return mapper.map(institution, InstitutionDto.class);
    }

    private Institution mapToEntity(InstitutionDto categoryDto) {
        return mapper.map(categoryDto, Institution.class);
    }
}

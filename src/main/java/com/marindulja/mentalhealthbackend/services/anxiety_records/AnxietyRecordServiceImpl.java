package com.marindulja.mentalhealthbackend.services.anxiety_records;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.anxietyrecord.AnxietyRecordReadDto;
import com.marindulja.mentalhealthbackend.dtos.anxietyrecord.AnxietyRecordWriteDto;
import com.marindulja.mentalhealthbackend.dtos.mapping.DTOMappings;
import com.marindulja.mentalhealthbackend.exceptions.InvalidInputException;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.AnxietyRecord;
import com.marindulja.mentalhealthbackend.models.PatientProfile;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.repositories.AnxietyRecordRepository;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnxietyRecordServiceImpl implements AnxietyRecordService {

    private final AnxietyRecordRepository anxietyRecordRepository;

    private final ProfileRepository userProfileRepository;

    private final DTOMappings mapper;

    @Override
    @Transactional
    public void registerAnxietyLevels(AnxietyRecordWriteDto anxietyRecordDto) {
        if (anxietyRecordDto.getAnxietyLevel() == null) {
            throw new InvalidInputException("Anxiety level should be defined");
        }

        Utilities.getCurrentUser()
                .flatMap(user -> userProfileRepository.findByUserId(user.getId()))
                .filter(PatientProfile.class::isInstance)
                .map(PatientProfile.class::cast)
                .ifPresentOrElse(patientProfile -> saveAnxietyRecord(anxietyRecordDto, patientProfile),
                        () -> {
                            throw new UnauthorizedException("Current user is not a patient");
                        });
    }

    private void saveAnxietyRecord(AnxietyRecordWriteDto anxietyRecordDto, PatientProfile patientProfile) {
        AnxietyRecord anxietyRecordToBeSaved = mapper.toAnxietyRecord(anxietyRecordDto);
        anxietyRecordToBeSaved.setAnxietyLevel(anxietyRecordDto.getAnxietyLevel());
        anxietyRecordToBeSaved.setUser(patientProfile);
        anxietyRecordToBeSaved.setRecordDate(LocalDateTime.now());
        anxietyRecordRepository.save(anxietyRecordToBeSaved);
        patientProfile.getAnxietyRecords().add(anxietyRecordToBeSaved);
        userProfileRepository.save(patientProfile);
    }

    @Override
    public List<AnxietyRecordReadDto> getAllOfCurrentPatient() {
        return Utilities.getCurrentUser()
                .flatMap(user -> userProfileRepository.findByUserId(user.getId()))
                .filter(PatientProfile.class::isInstance)
                .map(PatientProfile.class::cast)
                .map(PatientProfile::getAnxietyRecords)
                .map(records -> records.stream().map(mapper::toAnxietyRecordReadDto).collect(Collectors.toList()))
                .orElseThrow(() -> new EntityNotFoundException("Current user's patient profile not found"));
    }

    @Override
    public List<AnxietyRecordReadDto> viewPatientAnxietyLevels(long patientId) {
        User currentUser = Utilities.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found"));

        if (currentUser.getRole() == Role.THERAPIST) {
            if (Utilities.patientBelongsToTherapist(patientId, userProfileRepository))
                throw new UnauthorizedException("Patient with id " + patientId + " is not a patient of the therapist with id " + currentUser.getId());
        }

        if (currentUser.getRole() == Role.PATIENT && currentUser.getId() != patientId) {
            throw new UnauthorizedException("Current user is not authorized to view anxiety records of patient with id " + patientId);
        }

        PatientProfile patientProfile = userProfileRepository.findByUserId(patientId)
                .filter(PatientProfile.class::isInstance)
                .map(PatientProfile.class::cast)
                .orElseThrow(() -> new EntityNotFoundException("Profile of Patient with id " + patientId + " not found"));
        return patientProfile.getAnxietyRecords().stream()
                .map(mapper::toAnxietyRecordReadDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateAnxietyRecord(AnxietyRecordWriteDto anxietyRecord, long recordId) {
        PatientProfile patientProfile = Utilities.getCurrentUser()
                .flatMap(user -> userProfileRepository.findByUserId(user.getId()))
                .filter(PatientProfile.class::isInstance)
                .map(PatientProfile.class::cast).orElseThrow(() -> new UnauthorizedException("Current user is not a patient"));

        var anxietyRecordToUpdate = anxietyRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Anxiety record with id " + recordId + " not found"));
        anxietyRecordToUpdate.setAnxietyLevel(anxietyRecord.getAnxietyLevel());
        anxietyRecordToUpdate.setRecordDate(LocalDateTime.now());
        anxietyRecordRepository.save(anxietyRecordToUpdate);
        patientProfile.getAnxietyRecords().add(anxietyRecordToUpdate);
        userProfileRepository.save(patientProfile);
    }
}

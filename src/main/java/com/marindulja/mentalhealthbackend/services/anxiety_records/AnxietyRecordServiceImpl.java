package com.marindulja.mentalhealthbackend.services.anxiety_records;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.anxietyrecord.AnxietyRecordReadDto;
import com.marindulja.mentalhealthbackend.dtos.anxietyrecord.AnxietyRecordWriteDto;
import com.marindulja.mentalhealthbackend.dtos.mapping.ModelMappingUtility;
import com.marindulja.mentalhealthbackend.exceptions.InvalidInputException;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.AnxietyRecord;
import com.marindulja.mentalhealthbackend.models.PatientProfile;
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

    private final ModelMappingUtility mapper;

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
        AnxietyRecord anxietyRecordToBeSaved = mapper.map(anxietyRecordDto, AnxietyRecord.class);
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
                .map(records -> records.stream().map(record -> mapper.map(record, AnxietyRecordReadDto.class)).collect(Collectors.toList()))
                .orElseThrow(() -> new EntityNotFoundException("Current user's patient profile not found"));
    }

    @Override
    public List<AnxietyRecordReadDto> viewPatientAnxietyLevels(long patientId) {
        User currentUser = Utilities.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found"));

        if (Utilities.patientBelongsToTherapist(patientId, userProfileRepository))
            throw new UnauthorizedException("Patient with id " + patientId + " is not a patient of the therapist with id " + currentUser.getId());

        PatientProfile patientProfile = userProfileRepository.findByUserId(patientId)
                .filter(PatientProfile.class::isInstance)
                .map(PatientProfile.class::cast)
                .orElseThrow(() -> new EntityNotFoundException("Profile of Patient with id " + patientId + " not found"));
        return patientProfile.getAnxietyRecords().stream()
                .map(anxietyRecord -> mapper.map(anxietyRecord, AnxietyRecordReadDto.class))
                .collect(Collectors.toList());
    }
}

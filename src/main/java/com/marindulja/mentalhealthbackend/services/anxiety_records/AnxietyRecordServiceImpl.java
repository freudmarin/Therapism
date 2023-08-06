package com.marindulja.mentalhealthbackend.services.anxiety_records;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.AnxietyRecordDto;
import com.marindulja.mentalhealthbackend.exceptions.InvalidInputException;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.AnxietyRecord;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.models.UserProfile;
import com.marindulja.mentalhealthbackend.repositories.AnxietyRecordRepository;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AnxietyRecordServiceImpl implements AnxietyRecordService {

    private final AnxietyRecordRepository anxietyRecordRepository;

    private final UserRepository userRepository;

    private final ProfileRepository userProfileRepository;

    private final ModelMapper mapper = new ModelMapper();

    public AnxietyRecordServiceImpl(AnxietyRecordRepository anxietyRecordRepository, UserRepository userRepository, ProfileRepository userProfileRepository) {
        this.anxietyRecordRepository = anxietyRecordRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    @Transactional
    public AnxietyRecordDto registerAnxietyLevels(AnxietyRecordDto anxietyRecordDto) {
        User currentUser = Utilities.getCurrentUser().get();
        if (anxietyRecordDto.getAnxietyLevel() == null) {
            throw new InvalidInputException("Anxiety level should be defined");
        }
        UserProfile patientProfile = userProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Profile of Patient with id " + currentUser + "not found"));

        AnxietyRecord anxietyRecordToBeSaved = mapToEntity(anxietyRecordDto);
        anxietyRecordToBeSaved.setRecordDate(LocalDateTime.now());
        patientProfile.setAnxietyRecords(List.of(anxietyRecordToBeSaved));
        AnxietyRecord savedAnxietyRecord = anxietyRecordRepository.save(anxietyRecordToBeSaved);
        userProfileRepository.save(patientProfile);
        return mapToDTO(savedAnxietyRecord);
    }

    @Override
    public AnxietyRecordDto getById(long id) {
      return anxietyRecordRepository.findById(id).map(this::mapToDTO).orElseThrow(() ->
                new EntityNotFoundException("Anxiety Record with id " + id + " not found"));

    }

    @Override
    public List<AnxietyRecordDto> viewPatientAnxietyLevels(long patientId) {
        /*TODO : Filter patient records by date */
        User currentUser = Utilities.getCurrentUser().get();
        User patient = userRepository.findById(patientId).orElseThrow(() ->new EntityNotFoundException("Patient with id " + patientId + "doesn't exist" ));
        if (!Objects.equals(patient.getTherapist().getId(), currentUser.getId())) {
            throw new UnauthorizedException("Patient with id " + patientId + "is not a patient of the therapist with id " + currentUser.getId());
        }

        UserProfile patientProfile = userProfileRepository.findByUserId(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Profile of Patient with id " + patient + "not found"));

        return patientProfile.getAnxietyRecords().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private AnxietyRecordDto mapToDTO(AnxietyRecord anxietyRecord) {
        return mapper.map(anxietyRecord, AnxietyRecordDto.class);
    }

    private AnxietyRecord mapToEntity(AnxietyRecordDto anxietyRecordDto) {
        return mapper.map(anxietyRecordDto, AnxietyRecord.class);
    }
}

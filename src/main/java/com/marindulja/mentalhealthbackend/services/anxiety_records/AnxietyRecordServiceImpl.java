package com.marindulja.mentalhealthbackend.services.anxiety_records;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.AnxietyRecordReadDto;
import com.marindulja.mentalhealthbackend.dtos.AnxietyRecordWriteDto;
import com.marindulja.mentalhealthbackend.dtos.UserProfileWithUserDto;
import com.marindulja.mentalhealthbackend.dtos.mapping.ModelMappingUtility;
import com.marindulja.mentalhealthbackend.exceptions.InvalidInputException;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.AnxietyRecord;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.models.UserProfile;
import com.marindulja.mentalhealthbackend.repositories.AnxietyRecordRepository;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import com.marindulja.mentalhealthbackend.services.profiles.ProfileService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
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

    private final ProfileService profileService;

    private final ModelMappingUtility mapper;

    public AnxietyRecordServiceImpl(AnxietyRecordRepository anxietyRecordRepository, UserRepository userRepository, ProfileRepository userProfileRepository, ProfileService profileService, ModelMappingUtility mapper) {
        this.anxietyRecordRepository = anxietyRecordRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.profileService = profileService;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public UserProfileWithUserDto registerAnxietyLevels(AnxietyRecordWriteDto anxietyRecordDto) {
        User currentUser = Utilities.getCurrentUser().get();
        if (anxietyRecordDto.getAnxietyLevel() == null) {
            throw new InvalidInputException("Anxiety level should be defined");
        }
        UserProfile patientProfile = userProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Profile of Patient with id " + currentUser + "not found"));

        AnxietyRecord anxietyRecordToBeSaved = mapper.map(anxietyRecordDto, AnxietyRecord.class);
        anxietyRecordToBeSaved.setUser(patientProfile);
        anxietyRecordToBeSaved.setRecordDate(LocalDateTime.now());
        anxietyRecordRepository.save(anxietyRecordToBeSaved);
        patientProfile.getAnxietyRecords().add(anxietyRecordToBeSaved);
        userProfileRepository.save(patientProfile);
        return profileService.findByUserId(currentUser.getId());
    }

    @Override
    public List<AnxietyRecordReadDto> getAllOfCurrentUser() {
        User currentUser = Utilities.getCurrentUser().get();
        UserProfile currentUserProfile = userProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Profile of Patient with id " + currentUser + "not found"));
        return currentUserProfile.getAnxietyRecords().stream().map(anxietyRecord -> mapper.map(anxietyRecord, AnxietyRecordReadDto.class)).collect(Collectors.toList());
    }

    @Override
    public List<AnxietyRecordReadDto> viewPatientAnxietyLevels(long patientId) {
        /*TODO : Filter patient records by date */
        User currentUser = Utilities.getCurrentUser().get();
        User patient = userRepository.findById(patientId).orElseThrow(() ->new EntityNotFoundException("Patient with id " + patientId + " doesn't exist" ));
        if (patient.getTherapist() == null || !Objects.equals(patient.getTherapist().getId(), currentUser.getId())) {
            throw new UnauthorizedException("Patient with id " + patientId + " is not a patient of the therapist with id " + currentUser.getId());
        }

        UserProfile patientProfile = userProfileRepository.findByUserId(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Profile of Patient with id " + patient + "not found"));

        return patientProfile.getAnxietyRecords().stream().map(anxietyRecord -> mapper.map(anxietyRecord, AnxietyRecordReadDto.class)).collect(Collectors.toList());
    }

}

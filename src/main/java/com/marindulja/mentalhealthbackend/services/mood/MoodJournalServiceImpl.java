package com.marindulja.mentalhealthbackend.services.mood;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.MoodJournalReadDto;
import com.marindulja.mentalhealthbackend.dtos.MoodJournalWriteDto;
import com.marindulja.mentalhealthbackend.dtos.MoodTrendDto;
import com.marindulja.mentalhealthbackend.dtos.mapping.ModelMappingUtility;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.MoodJournal;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.repositories.MoodJournalRepository;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MoodJournalServiceImpl implements MoodJournalService {

    private final MoodJournalRepository moodJournalRepository;

    private final UserRepository userRepository;

    private final ProfileRepository profileRepository;
    private final ModelMappingUtility mapper;

    public MoodJournalServiceImpl(MoodJournalRepository moodEntryRepository, UserRepository userRepository, ProfileRepository profileRepository, ModelMappingUtility mapper) {
        this.moodJournalRepository = moodEntryRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.mapper = mapper;
    }


    public MoodJournalReadDto createMoodEntry(MoodJournalWriteDto moodEntryDTO) {
        final var moodJournalEntry = mapper.map(moodEntryDTO, MoodJournal.class);
        // Update mood entry fields
        moodJournalEntry.setUser(profileRepository.findById(Utilities.getCurrentUser().get().getId()).get());
        // Update other fields as needed
        moodJournalEntry.setEntryDate(moodEntryDTO.getEntryDate());
        MoodJournal savedMoodEntry = moodJournalRepository.save(moodJournalEntry);
        return mapper.map(savedMoodEntry, MoodJournalReadDto.class);
    }

    public List<MoodJournalReadDto> getMoodJournalsByPatient(Long userId) {
        final var currentUser = Utilities.getCurrentUser().get();
        if (currentUser.getRole() == Role.PATIENT && userId != currentUser.getId())
            throw new UnauthorizedException("Patient can view only his/her mood journal entries");
        else if (currentUser.getRole() == Role.THERAPIST && !Utilities.patientBelongsToTherapist(userId, profileRepository))
            throw new UnauthorizedException("Therapist can view only his/her patient's mood journal");
        final var moodEntries = moodJournalRepository.findAllByUserId(userId);
        return moodEntries.stream()
                .map(moodEntry -> mapper.map(moodEntry, MoodJournalReadDto.class))
                .collect(Collectors.toList());
    }

    public List<MoodJournalReadDto> getMoodJournalsByTherapist() {
        final var therapist = userRepository.findById(Utilities.getCurrentUser().get().getId()).get();
        final var patients = userRepository.findAllByTherapist(therapist);
        return patients.stream()
                .flatMap(patient -> moodJournalRepository.findAllByUserId(patient.getId())
                        .stream()
                        .map(moodEntry -> mapper.map(moodEntry, MoodJournalReadDto.class))
                )
                .collect(Collectors.toList());
    }

    public MoodJournalReadDto updateMoodJournal(Long moodEntryId, MoodJournalWriteDto updatedMoodJournalDTO) {
        // Check if the mood entry with the given ID exists
        final var existingMoodEntry = moodJournalRepository.findById(moodEntryId)
                .orElseThrow(() -> new EntityNotFoundException("MoodEntry with id :" + moodEntryId + "not found"));

        final var updatedMoodJournal = mapper.map(updatedMoodJournalDTO, MoodJournal.class);
        // Update mood entry fields
        existingMoodEntry.setEntryDate(updatedMoodJournal.getEntryDate());
        existingMoodEntry.setMoodLevel(updatedMoodJournal.getMoodLevel());
        existingMoodEntry.setMoodType(updatedMoodJournal.getMoodType());
        existingMoodEntry.setThoughts(updatedMoodJournal.getThoughts());
        existingMoodEntry.setActivities(updatedMoodJournal.getActivities());
        // Update other fields as needed

        MoodJournal savedMoodEntry = moodJournalRepository.save(existingMoodEntry);
        return mapper.map(savedMoodEntry, MoodJournalReadDto.class);
    }

    public List<MoodTrendDto> getMoodTrends(Long userId, ChronoUnit interval) {
        final var currentUser = Utilities.getCurrentUser().get();
        if (currentUser.getRole() == Role.PATIENT && userId != currentUser.getId())
            throw new UnauthorizedException("Patient can view only his/her mood journal trends");
        else if (currentUser.getRole() == Role.THERAPIST && !Utilities.patientBelongsToTherapist(userId, profileRepository))
            throw new UnauthorizedException("Therapist can view only his/her patient's mood trends");

        // Fetch mood entries for the user
        final var moodEntries = moodJournalRepository.findAllByUserId(userId);

        // Calculate aggregated mood trends based on the specified time interval
        return calculateMoodTrends(moodEntries, interval);
    }

    private List<MoodTrendDto> calculateMoodTrends(List<MoodJournal> moodJournals, ChronoUnit interval) {
        // Group mood entries by date based on the specified interval
        final var groupedEntries = moodJournals.stream()
                .collect(Collectors.groupingBy(entry -> truncateDate(entry.getEntryDate(), interval)));

        // Calculate average mood level for each date
        return groupedEntries.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<MoodJournal> entriesForDate = entry.getValue();
                    Double averageMoodLevel = calculateAverageMoodLevel(entriesForDate);
                    MoodTrendDto trendDTO = new MoodTrendDto();
                    trendDTO.setDate(date);
                    trendDTO.setAverageMoodLevel(averageMoodLevel);
                    return trendDTO;
                })
                .collect(Collectors.toList());
    }

    private LocalDate truncateDate(LocalDateTime dateTime, ChronoUnit interval) {
        // Truncate date based on the specified interval
        return switch (interval) {
            case DAYS -> dateTime.toLocalDate();
            case WEEKS -> dateTime.truncatedTo(ChronoUnit.WEEKS).toLocalDate();
            case MONTHS -> dateTime.withDayOfMonth(1).toLocalDate();
            default -> throw new IllegalArgumentException("Unsupported time interval");
        };
    }

    private Double calculateAverageMoodLevel(List<MoodJournal> entries) {
        // Extract mood levels from the list of MoodEntry objects and calculate the average
        final var average = entries.stream()
                .mapToDouble(MoodJournal::getMoodLevel)
                .average()
                .orElse(0.0);  // Default to 0 if there are no entries or average cannot be calculated

        // Format the result to have 2 digits after the decimal point
        return Double.parseDouble(String.format("%.2f", average)); // Default to 0 if there are no entries or average cannot be calculated
    }

    @Override
    public void deleteMoodEntry(Long therapyId) {
        final var moodEntry = moodJournalRepository.findById(therapyId).orElseThrow(() -> new EntityNotFoundException("MoodEntry with id " + therapyId + "not found"));
        moodEntry.setDeleted(true);
        moodJournalRepository.save(moodEntry);
    }
}

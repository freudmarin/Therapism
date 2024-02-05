package com.marindulja.mentalhealthbackend.services.mood;

import com.marindulja.mentalhealthbackend.dtos.MoodJournalDto;
import com.marindulja.mentalhealthbackend.dtos.MoodTrendDto;
import com.marindulja.mentalhealthbackend.models.MoodJournal;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.repositories.MoodJournalRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MoodJournalServiceImpl implements MoodJournalService {

    private final MoodJournalRepository moodJournalRepository;

    private final UserRepository userRepository;
    private final ModelMapper mapper = new ModelMapper();

    public MoodJournalServiceImpl(MoodJournalRepository moodEntryRepository, UserRepository userRepository) {
        this.moodJournalRepository = moodEntryRepository;
        this.userRepository = userRepository;
    }


    public MoodJournalDto createMoodEntry(MoodJournalDto moodEntryDTO) {
        MoodJournal moodEntry = this.mapToEntity(moodEntryDTO);
        MoodJournal savedMoodEntry = moodJournalRepository.save(moodEntry);
        return this.mapToDTO(savedMoodEntry);
    }

    public List<MoodJournalDto> getMoodJournalsByPatient(Long userId) {
        List<MoodJournal> moodEntries = moodJournalRepository.findAllByUserId(userId);
        return moodEntries.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<MoodJournalDto> getMoodJournalsByTherapist(Long therapistId) {
        User therapist = userRepository.findById(therapistId).orElseThrow(() -> new EntityNotFoundException("Therapist with id " + therapistId + "not found"));
        List<User> patients =  userRepository.findAllByTherapist(therapist);
        return patients.stream()
                .flatMap(patient -> moodJournalRepository.findAllByUserId(patient.getId())
                        .stream()
                        .map(this::mapToDTO)
                )
                .collect(Collectors.toList());
    }

    public MoodJournalDto updateMoodJournal(Long moodEntryId, MoodJournalDto updatedMoodJournalDTO) {
        // Check if the mood entry with the given ID exists
        MoodJournal existingMoodEntry = moodJournalRepository.findById(moodEntryId)
                .orElseThrow(() -> new EntityNotFoundException("MoodEntry with id :" + moodEntryId + "not found"));

        MoodJournal updatedMoodJournal = this.mapToEntity(updatedMoodJournalDTO);
        // Update mood entry fields
        existingMoodEntry.setEntryDate(updatedMoodJournal.getEntryDate());
        existingMoodEntry.setMoodLevel(updatedMoodJournal.getMoodLevel());
        existingMoodEntry.setMoodType(updatedMoodJournal.getMoodType());
        existingMoodEntry.setThoughts(updatedMoodJournal.getThoughts());
        existingMoodEntry.setActivities(updatedMoodJournal.getActivities());
        // Update other fields as needed

        MoodJournal savedMoodEntry = moodJournalRepository.save(existingMoodEntry);
        return this.mapToDTO(savedMoodEntry);
    }

    public List<MoodTrendDto> getMoodTrends(Long userId, ChronoUnit interval) {
        // Fetch mood entries for the user
        List<MoodJournal> moodEntries = moodJournalRepository.findAllByUserId(userId);

        // Calculate aggregated mood trends based on the specified time interval
        return calculateMoodTrends(moodEntries, interval);
    }

    private List<MoodTrendDto> calculateMoodTrends(List<MoodJournal> moodJournals, ChronoUnit interval) {
        // Group mood entries by date based on the specified interval
        Map<LocalDate, List<MoodJournal>> groupedEntries = moodJournals.stream()
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
        switch (interval) {
            case DAYS:
                return dateTime.toLocalDate();
            case WEEKS:
                return dateTime.truncatedTo(ChronoUnit.WEEKS).toLocalDate();
            case MONTHS:
                return dateTime.withDayOfMonth(1).toLocalDate();
            default:
                throw new IllegalArgumentException("Unsupported time interval");
        }
    }

    private Double calculateAverageMoodLevel(List<MoodJournal> entries) {
        // Extract mood levels from the list of MoodEntry objects and calculate the average
        return entries.stream()
                .mapToDouble(MoodJournal::getMoodLevel)
                .average()
                .orElse(0.0);  // Default to 0 if there are no entries or average cannot be calculated
    }

    @Override
    public void deleteMoodEntry(Long therapyId) {
        MoodJournal moodEntry = moodJournalRepository.findById(therapyId).orElseThrow(() -> new EntityNotFoundException("MoodEntry with id " + therapyId + "not found"));
        moodEntry.setDeleted(true);
        moodJournalRepository.save(moodEntry);
    }

    private MoodJournalDto mapToDTO(MoodJournal moodEntry) {
        return mapper.map(moodEntry, MoodJournalDto.class);
    }

    private MoodJournal mapToEntity(MoodJournalDto moodEntryDto) {
        return mapper.map(moodEntryDto, MoodJournal.class);
    }
}

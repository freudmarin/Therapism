package com.marindulja.mentalhealthbackend.services.mood;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.mapping.DTOMappings;
import com.marindulja.mentalhealthbackend.dtos.moodjounral.MoodJournalReadDto;
import com.marindulja.mentalhealthbackend.dtos.moodjounral.MoodJournalWriteDto;
import com.marindulja.mentalhealthbackend.dtos.moodjounral.MoodTrendDto;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.MoodJournal;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.repositories.MoodJournalRepository;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.ai.chat.client.ChatClient;
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
    private final DTOMappings mapper;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public MoodJournalServiceImpl(MoodJournalRepository moodJournalRepository, UserRepository userRepository, ProfileRepository profileRepository, DTOMappings mapper, ChatClient.Builder builder, ObjectMapper objectMapper) {
        this.moodJournalRepository = moodJournalRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.mapper = mapper;
        this.chatClient = builder.build();
        this.objectMapper = objectMapper;
    }

    public MoodJournalReadDto createMoodEntry(MoodJournalWriteDto moodEntryDTO) throws JsonProcessingException {
        final var moodJournalEntry = prepareMoodJournalEntry(moodEntryDTO);
        moodJournalEntry.setAiNotes(generateAiNotes(moodEntryDTO));
        MoodJournal savedMoodEntry = moodJournalRepository.save(moodJournalEntry);
        return mapper.toMoodJournalReadDto(savedMoodEntry);
    }

    private MoodJournal prepareMoodJournalEntry(MoodJournalWriteDto moodEntryDTO) {
        final var moodJournalEntry = mapper.toMoodJournal(moodEntryDTO);
        moodJournalEntry.setUser(profileRepository.findById(getCurrentUserOrThrow().getId()).orElseThrow(
                () -> new EntityNotFoundException("Profile with id " + getCurrentUserOrThrow().getId() + " not found")));
        moodJournalEntry.setEntryDate(LocalDateTime.now());
        return moodJournalEntry;
    }

    private String generateAiNotes(MoodJournalWriteDto moodEntryDTO) throws JsonProcessingException {
        String moodJournalJson = objectMapper.writeValueAsString(moodEntryDTO);
        String message = """
            Generate AI notes for this mood journal entry  {moodJournal}.
            Be specific to the mood level, mood type, thoughts and activities and suggest some activities to improve the mood.
            """;
        return chatClient.prompt()
                .user(m -> m.text(message).param("moodJournal", moodJournalJson))
                .call().content();
    }

    public List<MoodJournalReadDto> getMoodJournalsByPatient(Long patientId) {
        final var currentUser = getCurrentUserOrThrow();
        if (currentUser.getRole() == Role.PATIENT && !patientId.equals(currentUser.getId()))
            throw new UnauthorizedException("Patient can view only his/her mood journal entries");
        else if (currentUser.getRole() == Role.THERAPIST && !Utilities.patientBelongsToTherapist(patientId, profileRepository))
            throw new UnauthorizedException("Therapist can view only his/her patient's mood journal");
        final var moodEntries = moodJournalRepository.findAllByUserId(patientId);
        return moodEntries.stream()
                .map(mapper::toMoodJournalReadDto)
                .toList();
    }

    public List<MoodJournalReadDto> getMoodJournalsByTherapist() {
        final var therapist = getCurrentUserOrThrow();
        final var patients = userRepository.findAllByTherapist(therapist);
        return patients.stream()
                .flatMap(patient -> moodJournalRepository.findAllByUserId(patient.getId()).stream().filter(moodJournal -> !moodJournal.isSharedWithTherapist())
                        .map(mapper::toMoodJournalReadDto)
                )
                .toList();
    }

    public MoodJournalReadDto updateMoodJournal(Long moodEntryId, MoodJournalWriteDto updatedMoodJournalDTO) throws JsonProcessingException {
        // Check if the mood entry with the given ID exists
        final var existingMoodEntry = moodJournalRepository.findById(moodEntryId)
                .orElseThrow(() -> new EntityNotFoundException("MoodEntry with id :" + moodEntryId + "not found"));

        final var updatedMoodJournal = mapper.toMoodJournal(updatedMoodJournalDTO);
        // Update mood entry fields
        existingMoodEntry.setEntryDate(updatedMoodJournal.getEntryDate());
        existingMoodEntry.setMoodLevel(updatedMoodJournal.getMoodLevel());
        existingMoodEntry.setMoodType(updatedMoodJournal.getMoodType());
        existingMoodEntry.setThoughts(updatedMoodJournal.getThoughts());
        existingMoodEntry.setActivities(updatedMoodJournal.getActivities());
        existingMoodEntry.setAiNotes(generateAiNotes(updatedMoodJournalDTO));
        // Update other fields as needed

        MoodJournal savedMoodEntry = moodJournalRepository.save(existingMoodEntry);
        return mapper.toMoodJournalReadDto(savedMoodEntry);
    }

    @Override
    public void shareMoodJournalWithTherapist(Long moodEntryId, Long therapistId) {
        final var currentUser = getCurrentUserOrThrow();
        if(Utilities.therapistBelongsToPatient(therapistId, profileRepository))
            throw new UnauthorizedException("Therapist with id " + therapistId + " is not the therapist of the patient with id " + currentUser.getId());

        final var moodEntry = moodJournalRepository.findById(moodEntryId);
        moodEntry.ifPresentOrElse(moodJournal -> {
            moodJournal.setSharedWithTherapist(true);
            moodJournalRepository.save(moodJournal);
        }, () -> {
            throw new EntityNotFoundException("MoodEntry with id " + moodEntryId + "not found");
        });
    }

    public List<MoodTrendDto> getMoodTrends(Long patientId, ChronoUnit interval) {
        final var currentUser = getCurrentUserOrThrow();
        if (currentUser.getRole() == Role.PATIENT && !patientId.equals(currentUser.getId()))
            throw new UnauthorizedException("Patient can view only his/her mood journal trends");
        else if (currentUser.getRole() == Role.THERAPIST && !Utilities.patientBelongsToTherapist(patientId, profileRepository))
            throw new UnauthorizedException("Therapist can view only his/her patient's mood trends");
        else {
            // Fetch mood entries for the user
            final var moodEntries = moodJournalRepository.findAllByUserId(patientId);

            // Calculate aggregated mood trends based on the specified time interval
            return calculateMoodTrends(moodEntries, interval);
        }
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
                .toList();
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

    private User getCurrentUserOrThrow() {
        return Utilities.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
    }
}

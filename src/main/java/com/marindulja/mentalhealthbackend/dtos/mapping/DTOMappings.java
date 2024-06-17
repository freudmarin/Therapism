package com.marindulja.mentalhealthbackend.dtos.mapping;

import com.marindulja.mentalhealthbackend.dtos.anxietyrecord.AnxietyRecordReadDto;
import com.marindulja.mentalhealthbackend.dtos.anxietyrecord.AnxietyRecordWriteDto;
import com.marindulja.mentalhealthbackend.dtos.disorder.DisorderDto;
import com.marindulja.mentalhealthbackend.dtos.moodjounral.MoodJournalReadDto;
import com.marindulja.mentalhealthbackend.dtos.moodjounral.MoodJournalWriteDto;
import com.marindulja.mentalhealthbackend.dtos.profile.PatientProfileWriteDto;
import com.marindulja.mentalhealthbackend.dtos.profile.TherapistProfileWriteDto;
import com.marindulja.mentalhealthbackend.dtos.specialization.SpecializationDto;
import com.marindulja.mentalhealthbackend.dtos.symptom.SymptomDto;
import com.marindulja.mentalhealthbackend.dtos.task.AssignedTaskDto;
import com.marindulja.mentalhealthbackend.dtos.task.TaskDto;
import com.marindulja.mentalhealthbackend.dtos.therapysession.TherapySessionReadDto;
import com.marindulja.mentalhealthbackend.dtos.therapysession.TherapySessionWriteDto;
import com.marindulja.mentalhealthbackend.dtos.user.UserReadDto;
import com.marindulja.mentalhealthbackend.models.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DTOMappings {
    DTOMappings INSTANCE = Mappers.getMapper(DTOMappings.class);

    UserReadDto toUserDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "therapist", ignore = true)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "zoomJoinLinkUrl", ignore = true)
    @Mapping(target = "zoomStartLinkUrl", ignore = true)
    @Mapping(target = "meetingId", ignore = true)
    TherapySession toTherapySession(TherapySessionWriteDto therapySessionWriteDto);

    @Mapping(target = "sessionStatus", source = "status")
    TherapySessionReadDto toTherapySessionReadDto(TherapySession therapySession);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)

    AnxietyRecord toAnxietyRecord(AnxietyRecordWriteDto anxietyRecordDto);

    AnxietyRecordReadDto toAnxietyRecordReadDto(AnxietyRecord anxietyRecord);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedByUser", ignore = true)
    @Mapping(target = "assignedToUser", ignore = true)
    Task toTask(TaskDto taskDto);

    AssignedTaskDto toAssignedTaskDto(Task task);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "specializations", ignore = true)
    TherapistProfile toTherapistProfile(TherapistProfileWriteDto therapistProfileWriteDto);

    SpecializationDto toSpecializationDto(Specialization specialization);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "disorders", ignore = true)
    @Mapping(target = "symptoms", ignore = true)
    @Mapping(target = "anxietyRecords", ignore = true)
    PatientProfile toPatientProfile(PatientProfileWriteDto patientProfileWriteDto);

    SymptomDto toSymptomDto(Symptom symptom);

    DisorderDto toDisorderDto(Disorder disorder);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    MoodJournal toMoodJournal(MoodJournalWriteDto moodJournalDto);

    MoodJournalReadDto toMoodJournalReadDto(MoodJournal moodJournal);
}

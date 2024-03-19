package com.marindulja.mentalhealthbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AnxietyRecordWriteDto {
    private Integer anxietyLevel;
    private LocalDateTime recordDate;
}

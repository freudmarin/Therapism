package com.marindulja.mentalhealthbackend.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AnxietyRecordWriteDto {
    private Integer anxietyLevel;
    private LocalDateTime recordDate;
}

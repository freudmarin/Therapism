package com.marindulja.mentalhealthbackend.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AnxietyRecordDto {
    private Long id;
    private Integer anxietyLevel;
    private LocalDateTime recordDate;
}

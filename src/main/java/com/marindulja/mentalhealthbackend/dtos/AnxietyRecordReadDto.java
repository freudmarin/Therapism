package com.marindulja.mentalhealthbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnxietyRecordReadDto {
    private Long id;
    private Integer anxietyLevel;
    private LocalDateTime recordDate;
}

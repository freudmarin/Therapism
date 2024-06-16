package com.marindulja.mentalhealthbackend.dtos.anxietyrecord;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnxietyRecordWriteDto {
    private Integer anxietyLevel;
    private LocalDateTime recordDate;
}

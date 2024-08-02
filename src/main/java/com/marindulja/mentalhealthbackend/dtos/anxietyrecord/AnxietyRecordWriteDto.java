package com.marindulja.mentalhealthbackend.dtos.anxietyrecord;

import com.marindulja.mentalhealthbackend.validations.annotations.Between;
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
    @Between(min = 0, max = 10, message = "Anxiety level must be between 0 and 10")
    private Integer anxietyLevel;
    private LocalDateTime recordDate;
}

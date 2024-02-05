package com.marindulja.mentalhealthbackend.dtos;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MoodTrendDto {
    private LocalDate date;
    private Double averageMoodLevel;
}

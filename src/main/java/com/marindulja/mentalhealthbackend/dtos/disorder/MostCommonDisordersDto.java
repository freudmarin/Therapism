package com.marindulja.mentalhealthbackend.dtos.disorder;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MostCommonDisordersDto {
    private String disorderName;
    private Long disorderFrequency;
    private Double avgAnxietyLevel;
}

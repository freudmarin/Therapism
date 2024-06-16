package com.marindulja.mentalhealthbackend.dtos.symptom;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SymptomDto {
    private Long id;
    private String description;
}

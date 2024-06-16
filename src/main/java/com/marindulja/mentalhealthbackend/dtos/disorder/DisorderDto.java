package com.marindulja.mentalhealthbackend.dtos.disorder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DisorderDto {
    private Long id;
    private String name;
    private String description;
}

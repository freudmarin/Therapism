package com.marindulja.mentalhealthbackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisorderDto {
    private Long id;
    private String name;
    private String description;
}

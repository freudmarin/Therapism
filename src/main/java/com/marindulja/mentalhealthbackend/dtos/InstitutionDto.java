package com.marindulja.mentalhealthbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InstitutionDto {
    private Long id;

    private String name;
    private String address;

    private String contactNumber;

}

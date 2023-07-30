package com.marindulja.mentalhealthbackend.dtos;

import com.marindulja.mentalhealthbackend.models.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InstitutionDto {
    private Long id;

    private String name;
    private String address;

    private String contactNumber;
    private SubscriptionStatus subscriptionStatus;
    private LocalDateTime subscriptionExpiryDate;

}

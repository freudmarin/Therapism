package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;


@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "institutions")
@Where(clause = "is_deleted = false")
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "subscription_status")
    private SubscriptionStatus subscriptionStatus;

    @Column(name = "subscription_expiry_date")
    private LocalDateTime subscriptionExpiryDate;

    @Column(name = "is_deleted")
    private boolean isDeleted;
}

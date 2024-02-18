package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "anxiety_records")
public class AnxietyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_profile_id")
    private UserProfile user;

    @Column(name = "record_date", columnDefinition = "DATETIME")
    private LocalDateTime recordDate;
    private Integer anxietyLevel; // You can define the scale according to your needs
}

package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "anxiety_records")
public class AnxietyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(mappedBy = "anxietyRecords")
    private List<UserProfile> users = new ArrayList<>();

    private LocalDateTime recordDate;
    private Integer anxietyLevel; // You can define the scale according to your needs
}

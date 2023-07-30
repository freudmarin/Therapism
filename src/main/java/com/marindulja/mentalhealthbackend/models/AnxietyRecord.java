package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "anxiety_records")
public class AnxietyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(mappedBy = "anxietyRecords")
    private List<UserProfile> users = new ArrayList<>();

    private LocalDateTime recordDate;
    private int anxietyLevel; // You can define the scale according to your needs
}

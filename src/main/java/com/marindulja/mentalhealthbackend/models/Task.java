package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "assigned_by_user_id", nullable = false)
    private User assignedByUser;

    @ManyToOne
    @JoinColumn(name = "assigned_to_user_id", nullable = false)
    private User assignedToUser;

    private String description;

    private TaskStatus status; // "assigned", "in progress", "completed"
}

package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expiry_date", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime expiryDate;
}

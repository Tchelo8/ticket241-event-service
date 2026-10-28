package com.tchelo.event_service_ticket241.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// Événement mis en favori par un compte (X-User-Id : téléphone du JWT). Un seul favori par couple
// compte / événement ; les visiteurs sans compte gardent leurs favoris sur leur appareil.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "event_favorites",
        uniqueConstraints = @UniqueConstraint(name = "uk_favorite_user_event", columnNames = {"user_id", "event_id"}),
        indexes = @Index(name = "idx_favorite_user", columnList = "user_id"))
public class EventFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

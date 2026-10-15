package com.tchelo.event_service_ticket241.entity;

import com.tchelo.event_service_ticket241.entity.enums.SeatReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// Trace des places tenues pour une commande de ticket-service (une ligne par orderId).
// C'est elle qui rend la saga Kafka fiable : chaque message (réservé, payé, expiré, annulé) est
// appliqué par rapport à ce que CETTE commande détient réellement, donc un message rejoué, en
// retard ou incohérent ne peut jamais faire dériver les compteurs de la catégorie.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "seat_reservations", indexes = {
        @Index(name = "idx_seat_reservation_category", columnList = "ticket_category_id")
})
public class SeatReservation {

    @Id
    @Column(name = "order_id", length = 36)
    private String orderId;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "ticket_category_id")
    private Long ticketCategoryId;

    // Quantité demandée par ticket-service
    @Column(name = "requested_quantity", nullable = false)
    private Integer requestedQuantity;

    // Places encore réservées (non payées) pour cette commande
    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    // Places payées pour cette commande
    @Column(name = "sold_quantity", nullable = false)
    @Builder.Default
    private Integer soldQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatReservationStatus status;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

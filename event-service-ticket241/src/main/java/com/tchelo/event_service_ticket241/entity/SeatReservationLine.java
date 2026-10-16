package com.tchelo.event_service_ticket241.entity;

import jakarta.persistence.*;
import lombok.*;

// Places tenues par une commande dans UNE catégorie (une commande peut mélanger Standard et VIP)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "seat_reservation_lines", indexes = {
        @Index(name = "idx_seat_line_order", columnList = "order_id"),
        @Index(name = "idx_seat_line_category", columnList = "ticket_category_id")
})
public class SeatReservationLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private SeatReservation reservation;

    @Column(name = "ticket_category_id", nullable = false)
    private Long ticketCategoryId;

    @Column(name = "requested_quantity", nullable = false)
    private Integer requestedQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "sold_quantity", nullable = false)
    @Builder.Default
    private Integer soldQuantity = 0;
}

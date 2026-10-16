package com.tchelo.event_service_ticket241.entity;

import com.tchelo.event_service_ticket241.entity.enums.SeatReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Trace des places tenues pour une commande de ticket-service (une ligne par orderId, détaillée par
// catégorie dans SeatReservationLine).
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

    // Une ligne par catégorie de la commande. Les quantités ci-dessus sont les totaux de la commande ;
    // ticketCategoryId n'est renseigné que pour une commande d'une seule catégorie.
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<SeatReservationLine> lines = new ArrayList<>();

    // Commandes acceptées avant l'ajout des lignes (une seule catégorie, portée par la commande) :
    // leur ligne est créée au premier accès, avec les quantités de la commande
    public List<SeatReservationLine> resolveLines() {
        if (lines.isEmpty() && ticketCategoryId != null && status == SeatReservationStatus.ACCEPTED) {
            lines.add(SeatReservationLine.builder()
                    .reservation(this)
                    .ticketCategoryId(ticketCategoryId)
                    .requestedQuantity(requestedQuantity)
                    .reservedQuantity(reservedQuantity)
                    .soldQuantity(soldQuantity)
                    .build());
        }
        return lines;
    }

    // Ligne d'une catégorie ; sans catégorie (ancien message), la seule ligne si la commande n'en a qu'une
    public Optional<SeatReservationLine> lineFor(Long categoryId) {
        List<SeatReservationLine> resolved = resolveLines();
        if (categoryId == null) {
            return resolved.size() == 1 ? Optional.of(resolved.get(0)) : Optional.empty();
        }
        return resolved.stream().filter(line -> categoryId.equals(line.getTicketCategoryId())).findFirst();
    }

    // Recalcule les totaux de la commande à partir de ses lignes
    public void recalculateTotals() {
        reservedQuantity = lines.stream().mapToInt(SeatReservationLine::getReservedQuantity).sum();
        soldQuantity = lines.stream().mapToInt(SeatReservationLine::getSoldQuantity).sum();
    }
}

package com.tchelo.event_service_ticket241.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCancelledEvent {

    private String eventId;
    private Long ticketId;
    private String ticketNumber;
    private String orderId;
    // Statut du billet avant l'annulation : RESERVED → on libère une place réservée,
    // PAID → on libère une place vendue
    private String previousStatus;
    private Long eventIdLong;
    private Long categoryId;
    private Integer quantity;
    private String userId;
    private String reason;
    private LocalDateTime cancelledAt;
    private LocalDateTime timestamp;
}

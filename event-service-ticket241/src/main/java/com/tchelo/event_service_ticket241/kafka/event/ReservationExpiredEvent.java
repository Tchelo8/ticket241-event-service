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
public class ReservationExpiredEvent {

    private String eventId;
    private Long ticketId;
    private String ticketNumber;
    private Long eventIdLong;
    private Long categoryId;
    private Integer quantity;
    private String orderId;
    private LocalDateTime expiredAt;
    private LocalDateTime timestamp;
}

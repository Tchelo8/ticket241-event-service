package com.tchelo.event_service_ticket241.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCategoryResponseDto {

    private Long id;
    private UUID uuid;
    private String name;
    private String description;
    private BigDecimal price;

    private Integer totalSeats;
    private Integer availableSeats;
    private Integer reservedSeats;
    private Integer soldSeats;

    private Integer displayOrder;
    private Boolean isActive;
    private Integer maxTicketsPerOrder;

    private String benefits;
    private String colorCode;

    // Information calculée par le système automatically
    private Boolean hasAvailableSeats;
}

package com.tchelo.event_service_ticket241.dto.response;

import com.tchelo.event_service_ticket241.entity.enums.EventCategory;
import com.tchelo.event_service_ticket241.entity.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

//dto mis en place pour mes listes , renvoyez une version résumée de mes évèments pour ne pas saturer les requêtes api.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSummaryDto {

    private Long id;
    private UUID uuid;
    private String name;
    private String slug;

    private EventCategory category;
    private String subCategory;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private String venueName;
    private Long cityId;
    private String cityName;
    private String cityCountry;

    private Integer availableSeats;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private String coverImageUrl;

    private EventStatus status;
    private Boolean isFeatured;
    private Boolean isPromoted;

    private Integer viewCount;
    private Double averageRating;

    // Informations calculées
    private Boolean isSaleOpen;
    private Boolean isPastEvent;
    private Boolean isSoldOut;
}
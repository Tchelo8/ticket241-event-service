package com.tchelo.event_service_ticket241.dto.response;

import com.tchelo.event_service_ticket241.entity.enums.EventStatus;
import com.tchelo.event_service_ticket241.entity.enums.ValidationMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//dto mis en place pour mes listes , renvoyez une version résumée de mes évèments pour ne pas saturer les requêtes api.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSummaryDto {

    private Long id;
    private String uuid;
    private String name;
    private String slug;
    private String fullDescription;

    private Long categoryId;
    private String category;
    private String categoryDisplayName;
    private String subCategory;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime doorsOpenTime;
    private LocalDateTime saleStartDate;
    private LocalDateTime saleEndDate;

    private String venueName;
    private String venueAddress;
    private Long cityId;
    private String cityName;
    private String cityCountry;
    private String venueAccessInfo;

    private Integer totalSeats;
    private Integer availableSeats;
    private Integer reservedSeats;
    private Integer soldSeats;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private Long structureId;
    private String structureName;  // Récupéré depuis Structure directement

    private String organizerName;
    private String organizerPhone;

    private String coverImageUrl;

    private EventStatus eventStatus;
    private Boolean isFeatured;
    private Boolean isPromoted;
    private Boolean isOnline;
    private String onlineMeetingUrl;
    private Boolean isPublic;
    private Boolean isActive;

    private Integer minAge;
    private String termsAndConditions;
    private String cancellationPolicy;
    private Boolean allowRefund;
    private Integer refundDeadlineDays;
    private Integer maxTicketsPerUser;

    private Integer viewCount;
    private Integer favoriteCount;
    private Integer shareCount;
    private Double averageRating;
    private Integer reviewCount;

    private ValidationMode validationMode;

    private String createdBy;
    private String updatedBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime publishedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isCancelled;

    // Liste des catégories de tickets
    @Builder.Default
    private List<TicketCategoryResponseDto> ticketCategories = new ArrayList<>();

    // Informations calculées
    private Boolean isSaleOpen;
    private Boolean isPastEvent;
    private Boolean isSoldOut;
}
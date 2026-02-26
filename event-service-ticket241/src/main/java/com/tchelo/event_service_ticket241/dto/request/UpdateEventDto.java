package com.tchelo.event_service_ticket241.dto.request;

import com.tchelo.event_service_ticket241.entity.enums.EventCategory;
import com.tchelo.event_service_ticket241.entity.enums.EventStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventDto {

    @Size(min = 3, max = 200, message = "Le nom doit contenir entre 3 et 200 caractères")
    private String name;

    @Size(min = 10, message = "La description doit contenir au moins 10 caractères")
    private String fullDescription;

    private EventCategory category;

    @Size(max = 100)
    private String subCategory;

    @Future(message = "La date de début doit être dans le futur, pas une date déjà passée.")
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime doorsOpenTime;

    private LocalDateTime saleStartDate;

    private LocalDateTime saleEndDate;

    @Size(max = 200)
    private String venueName;

    @Size(max = 200)
    private String venueAddress;

    private Long cityId;

    @Size(max = 1000)
    private String venueAccessInfo;

    @Size(max = 100)
    private String organizerName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Format de téléphone invalide, supprimez les espaces ou les symbôles.")
    private String organizerPhone;

    @Size(max = 500)
    private String coverImageUrl;

    private EventStatus status;

    private Boolean isPublic;

    private Boolean isFeatured;

    private Boolean isPromoted;

    private Boolean isOnline;

    @Size(max = 500)
    private String onlineMeetingUrl;

    @Min(value = 0)
    @Max(value = 99)
    private Integer minAge;

    @Size(max = 2000)
    private String termsAndConditions;

    @Size(max = 1000)
    private String cancellationPolicy;

    private Boolean allowRefund;

    @Min(value = 0)
    private Integer refundDeadlineDays;

    @Min(value = 1)
    @Max(value = 100)
    private Integer maxTicketsPerUser;
}

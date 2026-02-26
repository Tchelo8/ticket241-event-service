package com.tchelo.event_service_ticket241.dto.request;

import com.tchelo.event_service_ticket241.entity.enums.EventCategory;
import com.tchelo.event_service_ticket241.entity.enums.ValidationMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEventDto {

    @NotBlank(message = "Le nom de l'événement est obligatoire")
    @Size(min = 3, max = 200, message = "Le nom doit contenir entre 3 et 200 caractères")
    private String name;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, message = "La description doit contenir au moins 10 caractères")
    private String fullDescription;

    @NotNull(message = "La catégorie est obligatoire")
    private EventCategory category;

    @Size(max = 100, message = "La sous-catégorie ne peut pas dépasser 100 caractères")
    private String subCategory;

    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime doorsOpenTime;

    private LocalDateTime saleStartDate;

    private LocalDateTime saleEndDate;

    @NotBlank(message = "Le nom du lieu est obligatoire")
    @Size(max = 200, message = "Le nom du lieu ne peut pas dépasser 200 caractères,essayez de le raccourcir")
    private String venueName;

    @Size(max = 200, message = "L'adresse ne peut pas dépasser 200 caractères, essayez de raccourcir")
    private String venueAddress;

    @NotNull(message = "La ville est obligatoire")
    private Long cityId;

    @Size(max = 1000, message = "Les informations d'accès ne peuvent pas dépasser 1000 caractères,essayez de raccourcir")
    private String venueAccessInfo;

    @NotNull(message = "Le nombre total de places est obligatoire")
    @Min(value = 1, message = "Le nombre de places doit être au moins de 1 place")
    private Integer totalSeats;

    @NotNull(message = "La structure mère de l'évènement est obligatoire")
    private Long structureId;

    @Size(max = 100, message = "Le nom de l'organisateur ne peut pas dépasser 100 caractères, essayez de le raccourcir")
    private String organizerName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Format de téléphone invalide, enlevez les espaces ou les caractères spéciaux.")
    private String organizerPhone;

    @Size(max = 500, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
    private String coverImageUrl;

    private Boolean isPublic = true;

    private Boolean isOnline = false;

    @Size(max = 500, message = "L'URL de la réunion ne peut pas dépasser 500 caractères")
    private String onlineMeetingUrl;

    @Min(value = 0, message = "L'âge minimum ne peut pas être négatif")
    @Max(value = 99, message = "L'âge minimum ne peut pas dépasser 99")
    private Integer minAge;

    @Size(max = 2000, message = "Les termes et conditions ne peuvent pas dépasser 2000 caractères, essayez de réduire")
    private String termsAndConditions;

    @Size(max = 1000, message = "La politique d'annulation ne peut pas dépasser 1000 caractères, essayez de réduire")
    private String cancellationPolicy;

    private Boolean allowRefund = true;

    @Min(value = 0, message = "Le délai de remboursement ne peut pas être négatif")
    private Integer refundDeadlineDays = 7;

    @Min(value = 1, message = "Le nombre maximum de tickets par utilisateur doit être au moins de 1 ticket")
    @Max(value = 100, message = "Le nombre maximum de tickets par utilisateur ne peut pas dépasser 100 tickets")
    private Integer maxTicketsPerUser = 10;

    private ValidationMode validationMode = ValidationMode.AUTO;

    // Liste des catégories de tickets
    @Valid
    @NotEmpty(message = "Au moins une catégorie de ticket est obligatoire pour valider votre achat")
    private List<CreateTicketCategoryDto> ticketCategories = new ArrayList<>();
}

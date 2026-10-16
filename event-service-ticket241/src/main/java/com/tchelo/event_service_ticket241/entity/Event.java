package com.tchelo.event_service_ticket241.entity;


import com.tchelo.event_service_ticket241.entity.enums.EventStatus;
import com.tchelo.event_service_ticket241.entity.enums.ValidationMode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_event_status", columnList = "status"),
        @Index(name = "idx_event_category", columnList = "category_id"),
        @Index(name = "idx_event_start_date", columnList = "start_date"),
        @Index(name = "idx_event_structure", columnList = "structure_id"),
        @Index(name = "idx_event_slug", columnList = "slug"),
        @Index(name = "idx_event_city", columnList = "city_id"),
        @Index(name = "idx_event_uuid", columnList = "uuid"),
        @Index(name = "idx_event_is_public", columnList = "is_public"),
        @Index(name = "idx_event_is_active", columnList = "is_active")
})
public class Event extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(unique = true, nullable = false, length = 250)
    private String slug;

    @Column(name = "full_description", columnDefinition = "TEXT")
    private String fullDescription;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "sub_category", length = 100)
    private String subCategory;


    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "doors_open_time")
    private LocalDateTime doorsOpenTime;

    @Column(name = "sale_start_date")
    private LocalDateTime saleStartDate;

    @Column(name = "sale_end_date")
    private LocalDateTime saleEndDate;


    @Column(name = "venue_name", nullable = false, length = 200)
    private String venueName;

    @Column(name = "venue_address", length = 200)
    private String venueAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(name = "venue_access_info", columnDefinition = "TEXT")
    private String venueAccessInfo;


    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(name = "reserved_seats", nullable = false)
    private Integer reservedSeats = 0;

    @Column(name = "sold_seats", nullable = false)
    private Integer soldSeats = 0;

    @Column(name = "min_price", precision = 10, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 10, scale = 2)
    private BigDecimal maxPrice;


    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TicketCategory> ticketCategories = new ArrayList<>();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "structure_id", nullable = false)
    private Structure structure;

    @Column(name = "organizer_name", length = 100)
    private String organizerName;

    @Column(name = "organizer_phone", length = 50)
    private String organizerPhone;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EventStatus eventStatus = EventStatus.DRAFT;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "is_promoted")
    @Builder.Default
    private Boolean isPromoted = false;

    @Column(name = "is_online")
    @Builder.Default
    private Boolean isOnline = false;

    @Column(name = "online_meeting_url", length = 500)
    private String onlineMeetingUrl;

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = true;  // Visible dans les listes publiques

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;  // Événement actif dès sa création

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @Column(name = "cancellation_policy", columnDefinition = "TEXT")
    private String cancellationPolicy;

    @Column(name = "allow_refund")
    @Builder.Default
    private Boolean allowRefund = false;

    @Column(name = "refund_deadline_days")
    @Builder.Default
    private Integer refundDeadlineDays = 7;

    @Column(name = "max_tickets_per_user")
    @Builder.Default
    private Integer maxTicketsPerUser = 10;


    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "favorite_count")
    @Builder.Default
    private Integer favoriteCount = 0;

    @Column(name = "share_count")
    @Builder.Default
    private Integer shareCount = 0;

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;


    @Column(name = "validation_mode", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ValidationMode validationMode = ValidationMode.AUTO;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "is_cancelled")
    @Builder.Default
    private Boolean isCancelled = false;


    @Version
    private Long version;


//    @PrePersist
//    protected void onCreate() {
//        if (this.uuid == null) {
//            this.uuid = UUID.randomUUID();
//        }
//        if (this.availableSeats == null && this.totalSeats != null) {
//            this.availableSeats = this.totalSeats;
//        }
//        if (this.slug == null && this.name != null) {
//            this.slug = generateSlug(this.name);
//        }
//    }

    @PreUpdate
    protected void onUpdate() {
        // Recalculer les places disponibles
        this.availableSeats = this.totalSeats - this.soldSeats - this.reservedSeats;
    }

    // Les places se gèrent par catégorie de billet : les compteurs de l'événement en sont la somme.
    // À appeler après tout mouvement de places sur une catégorie, sinon l'événement ne passe
    // jamais "complet" et isSaleOpen() se base sur des chiffres faux.
    public void recalculateSeatsFromCategories() {
        this.reservedSeats = ticketCategories.stream().mapToInt(TicketCategory::getReservedSeats).sum();
        this.soldSeats = ticketCategories.stream().mapToInt(TicketCategory::getSoldSeats).sum();
        this.availableSeats = this.totalSeats - this.soldSeats - this.reservedSeats;
    }


    // Ajouter une catégorie de ticket à l'événement
    public void addTicketCategory(TicketCategory ticketCategory) {
        ticketCategories.add(ticketCategory);
        ticketCategory.setEvent(this);
    }


    // Retirer une catégorie de ticket
    public void removeTicketCategory(TicketCategory ticketCategory) {
        ticketCategories.remove(ticketCategory);
        ticketCategory.setEvent(null);
    }

    // Raison pour laquelle on ne peut pas réserver de billet sur cet événement, ou null si la vente
    // est ouverte. Source unique de vérité, utilisée par la réservation Kafka et exposée à
    // ticket-service (saleClosedReason dans EventResponseDto) pour refuser au plus tôt.
    // Ne tient pas compte des places : elles se vérifient par catégorie.
    public String getSaleClosedReason() {
        if (this.eventStatus != EventStatus.PUBLISHED) {
            return "L'événement n'est pas ouvert à la vente (statut : " + this.eventStatus + ")";
        }
        if (!Boolean.TRUE.equals(this.isActive) || Boolean.TRUE.equals(this.isCancelled)) {
            return "L'événement n'est plus disponible";
        }
        LocalDateTime now = LocalDateTime.now();
        if (this.saleStartDate != null && now.isBefore(this.saleStartDate)) {
            return "La vente des billets n'a pas encore commencé";
        }
        if (this.saleEndDate != null && now.isAfter(this.saleEndDate)) {
            return "La vente des billets est terminée";
        }
        if (isPastEvent()) {
            return "L'événement a déjà commencé";
        }
        return null;
    }

    // Visible sur les pages publiques : tout sauf les brouillons, les événements en attente de
    // validation et les événements supprimés. Un événement annulé ou reporté reste consultable
    // (les détenteurs de billets doivent pouvoir le voir).
    public boolean isPubliclyVisible() {
        return Boolean.TRUE.equals(this.isActive)
                && this.eventStatus != EventStatus.DRAFT
                && this.eventStatus != EventStatus.PENDING_APPROVAL;
    }

    //Vérifie si des billets sont encore disponibles
    public boolean hasAvailableSeats(int requestedSeats) {
        return this.availableSeats != null && this.availableSeats >= requestedSeats;
    }

    //Vérifie si les ventes sont ouvertes
    public boolean isSaleOpen() {
        LocalDateTime now = LocalDateTime.now();
        return this.eventStatus == EventStatus.PUBLISHED
                && this.isActive
                && this.isPublic
                && (this.saleStartDate == null || now.isAfter(this.saleStartDate))
                && (this.saleEndDate == null || now.isBefore(this.saleEndDate))
                && this.availableSeats > 0;
    }

    //Vérifie si l'événement est passé
    public boolean isPastEvent() {
        return this.startDate != null && this.startDate.isBefore(LocalDateTime.now());
    }

    //Génère un slug URL-friendly
    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[àáâãäå]", "a")
                .replaceAll("[èéêë]", "e")
                .replaceAll("[ìíîï]", "i")
                .replaceAll("[òóôõö]", "o")
                .replaceAll("[ùúûü]", "u")
                .replaceAll("[ç]", "c")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "")
                + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}

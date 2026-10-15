package com.tchelo.event_service_ticket241.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ticket_categories", indexes = {
        @Index(name = "idx_ticket_category_event", columnList = "event_id"),
        @Index(name = "idx_ticket_category_name", columnList = "name")
})
public class TicketCategory extends BaseEntity {


//    @Column(nullable = false, unique = true, updatable = false)
//    @Builder.Default
//    private UUID uuid = UUID.randomUUID();



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;


    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;  // Prix du ticket dans cette catégorie


    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;  // Nombre total de places pour cette catégorie pendant cette evénement

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(name = "reserved_seats", nullable = false)
    @Builder.Default
    private Integer reservedSeats = 0;

    @Column(name = "sold_seats", nullable = false)
    @Builder.Default
    private Integer soldSeats = 0;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;  // Ordre d'affichage (VIP en premier, etc.)

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "max_tickets_per_order")
    @Builder.Default
    private Integer maxTicketsPerOrder = 10;


    @Column(length = 500)
    private String benefits;  // Avantages (Ex: "Accès backstage, boisson offerte")

    @Column(name = "color_code", length = 7)
    private String colorCode;  // Code couleur pour l'affichage (#FF5733)


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
//    }

    @PreUpdate
    protected void onUpdate() {
        // fonction qui permettra de recalculer les places disponibles
        this.availableSeats = this.totalSeats - this.soldSeats - this.reservedSeats;
    }


    // fonction qui vérifie si des places sont disponibles
    public boolean hasAvailableSeats(int requestedSeats) {
        return this.isActive
                && this.availableSeats != null
                && this.availableSeats >= requestedSeats;
    }

    @PrePersist
    protected void onCreate() {
        super.isArchived = false;

        // Générer UUID automatiquement
        if (this.uuid == null || this.uuid.isEmpty()) {
            this.uuid = UUID.randomUUID().toString();
        }

        if (this.availableSeats == null && this.totalSeats != null) {
            this.availableSeats = this.totalSeats;
        }
    }

    // Les 4 mouvements de places possibles. Chacun vérifie qu'il ne rend aucun compteur négatif,
    // puis recalcule availableSeats à partir des compteurs (jamais d'incrément "à la main" qui
    // pourrait dériver) : disponibles = total - vendues - réservées.

    // Réservation pendant la commande (avant paiement)
    public void reserveSeats(int quantity) {
        if (!hasAvailableSeats(quantity)) {
            throw new IllegalStateException(String.format(
                    "Pas assez de places disponibles dans la catégorie %s (disponibles : %d, demandées : %d)",
                    this.name, this.availableSeats, quantity));
        }
        this.reservedSeats += quantity;
        recalculateAvailableSeats();
    }

    // Confirme la vente après le paiement via airtel ou moov money : réservées → vendues
    public void confirmSale(int quantity) {
        if (this.reservedSeats < quantity) {
            throw new IllegalStateException("Impossible de confirmer plus de places que réservées dans " + this.name);
        }
        this.reservedSeats -= quantity;
        this.soldSeats += quantity;
        recalculateAvailableSeats();
    }

    // Libère des places réservées (réservation expirée ou annulée avant paiement)
    public void cancelReservation(int quantity) {
        if (this.reservedSeats < quantity) {
            throw new IllegalStateException("Impossible de libérer plus de places que réservées dans " + this.name);
        }
        this.reservedSeats -= quantity;
        recalculateAvailableSeats();
    }

    // Libère des places vendues (billet payé puis annulé)
    public void cancelSale(int quantity) {
        if (this.soldSeats < quantity) {
            throw new IllegalStateException("Impossible d'annuler plus de tickets que vendu dans " + this.name);
        }
        this.soldSeats -= quantity;
        recalculateAvailableSeats();
    }

    private void recalculateAvailableSeats() {
        this.availableSeats = this.totalSeats - this.soldSeats - this.reservedSeats;
    }
}

package com.tchelo.event_service_ticket241.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cities", indexes = {
        @Index(name = "idx_city_name", columnList = "name"),
        @Index(name = "idx_city_country", columnList = "country")
})
public class City extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String country;

    @Column(name = "country_code", length = 3)
    private String countryCode;

    @Column(length = 100)
    private String region;
    @Column(name = "postal_code", length = 20)
    private String postalCode;

    private Double latitude;

    private Double longitude;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "event_count")
    @Builder.Default
    private Integer eventCount = 0;  // Nombre d'événements dans cette ville

    @PrePersist
    protected void onCreate() {
        super.isArchived = false;

        if (this.uuid == null || this.uuid.isEmpty()) {
            this.uuid = java.util.UUID.randomUUID().toString();
        }
    }
}

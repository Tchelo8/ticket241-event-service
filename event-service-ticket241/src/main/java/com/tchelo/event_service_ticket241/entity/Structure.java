package com.tchelo.event_service_ticket241.entity;

import com.tchelo.event_service_ticket241.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "structures")
public class Structure extends BaseEntity {

    @Column(nullable = false)
    private String nom;

    private String location;
    private String lieu;

    private String gerant;
    private String contact;
    private String contactGerant;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(unique = true, nullable = true)
    private String email;

    private String type;

    private String logoUrl;

    private Boolean isVerified = false;

    private Integer validatedEventCount = 0;

    private String typeContrat;
}

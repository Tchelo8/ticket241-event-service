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

    // Code d'invitation des admins de structure : seul son hash SHA-256 est stocké, le code en clair
    // n'est montré qu'une fois à l'admin Ticket241 qui le génère. Usage unique, durée limitée.
    @Column(name = "invitation_code_hash", length = 64)
    private String invitationCodeHash;

    @Column(name = "invitation_code_expires_at")
    private java.time.LocalDateTime invitationCodeExpiresAt;
}

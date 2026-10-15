package com.tchelo.event_service_ticket241.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StructureResponseDTO {
    private Long id;
    private String uuid;
    private String nom;
    private String email;
    private String location;
    private String lieu;
    private String gerant;
    private String contact;
    private String contactGerant;
    private String description;
    private String type;
    private String logoUrl;
    private String typeContrat;
    private Boolean status;
    private Boolean isVerified;
    private Integer validatedEventCount;
}
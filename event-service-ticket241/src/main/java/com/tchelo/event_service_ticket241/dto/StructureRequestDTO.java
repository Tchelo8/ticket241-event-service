package com.tchelo.event_service_ticket241.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StructureRequestDTO {

    private String nom;
    private String location;
    private String lieu;
    private String gerant;
    private String contact;
    private String contactGerant;
    private String description;
    private String email;
    private String type;
    private String logoUrl;
    private String typeContrat;
}

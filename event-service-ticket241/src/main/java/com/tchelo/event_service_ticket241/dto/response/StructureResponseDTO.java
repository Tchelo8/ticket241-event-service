package com.tchelo.event_service_ticket241.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class StructureResponseDTO {
    private UUID id;
    private String nom;
    private String email;
    private Boolean status;
    private Boolean isVerified;
}

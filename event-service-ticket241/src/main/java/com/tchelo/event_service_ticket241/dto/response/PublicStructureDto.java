package com.tchelo.event_service_ticket241.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Fiche publique d'un organisateur (annuaire du site) : uniquement ce qu'un visiteur peut voir.
// Jamais le gérant, les contacts, l'email ni le contrat (ceux-là restent dans StructureResponseDTO).
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicStructureDto {

    private Long id;
    private String uuid;
    private String name;
    private String type;
    private String city;
    private String venue;
    private String description;
    private String logoUrl;
    private long upcomingEventCount;
}

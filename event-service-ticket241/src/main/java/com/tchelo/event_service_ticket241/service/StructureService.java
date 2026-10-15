package com.tchelo.event_service_ticket241.service;

import com.tchelo.event_service_ticket241.dto.request.StructureRequestDTO;
import com.tchelo.event_service_ticket241.dto.response.InvitationCodeCheckResponseDto;
import com.tchelo.event_service_ticket241.dto.response.InvitationCodeResponseDto;
import com.tchelo.event_service_ticket241.dto.response.StructureResponseDTO;
import com.tchelo.event_service_ticket241.security.UserContext;

import java.util.List;

public interface StructureService {

    // Admin Ticket241 uniquement
    StructureResponseDTO createStructure(StructureRequestDTO dto, UserContext user);

    StructureResponseDTO getStructureById(Long id);

    StructureResponseDTO getStructureByUuid(String uuid);

    List<StructureResponseDTO> getAllStructures();

    // Admin Ticket241 ou admin de cette structure
    StructureResponseDTO updateStructure(Long id, StructureRequestDTO dto, UserContext user);

    // Admin Ticket241 uniquement
    StructureResponseDTO verifyStructure(Long id, UserContext user);

    // Admin Ticket241 uniquement, et seulement si la structure n'a aucun événement
    void deleteStructure(Long id, UserContext user);

    // Admin Ticket241 uniquement : génère un nouveau code (l'ancien devient invalide)
    InvitationCodeResponseDto generateInvitationCode(Long id, UserContext user);

    // Appels internes d'auth-service : vérifie le code sans le consommer
    InvitationCodeCheckResponseDto checkInvitationCode(Long id, String code);

    // Appels internes d'auth-service : vérifie et consomme le code (usage unique)
    InvitationCodeCheckResponseDto consumeInvitationCode(Long id, String code);
}

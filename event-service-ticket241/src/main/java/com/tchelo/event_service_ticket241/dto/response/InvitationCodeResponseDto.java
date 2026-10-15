package com.tchelo.event_service_ticket241.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Code d'invitation en clair : renvoyé UNE seule fois, à sa génération, à l'admin Ticket241
// qui le transmet à l'admin de la structure
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationCodeResponseDto {

    private Long structureId;
    private String structureName;
    private String invitationCode;
    private LocalDateTime expiresAt;
    private String message;
}

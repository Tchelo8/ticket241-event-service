package com.tchelo.event_service_ticket241.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Résultat de la vérification / consommation d'un code d'invitation (appel interne d'auth-service)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationCodeCheckResponseDto {

    private boolean valid;
    private String message;
}

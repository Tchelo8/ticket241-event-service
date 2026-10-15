package com.tchelo.event_service_ticket241.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Code d'invitation saisi par un futur admin de structure, transmis par auth-service (appel interne)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationCodeCheckRequestDto {

    @NotBlank(message = "Le code d'invitation est obligatoire")
    private String code;
}

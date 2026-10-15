package com.tchelo.event_service_ticket241.controller;

import com.tchelo.event_service_ticket241.dto.request.InvitationCodeCheckRequestDto;
import com.tchelo.event_service_ticket241.dto.response.InvitationCodeCheckResponseDto;
import com.tchelo.event_service_ticket241.service.StructureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Endpoints service-à-service (clé X-Internal-Api-Key exigée par InternalApiKeyFilter).
// Non routés par la gateway : appelés par auth-service via Eureka lors de l'inscription d'un admin de structure.
@RestController
@RequestMapping("/internal/structures")
@RequiredArgsConstructor
@Slf4j
public class InternalStructureController {

    private final StructureService structureService;

    // Vérifie le code sans le consommer (refus rapide avant de créer le compte)
    @PostMapping("/{id}/invitation-code/check")
    public ResponseEntity<InvitationCodeCheckResponseDto> checkInvitationCode(
            @PathVariable Long id,
            @Valid @RequestBody InvitationCodeCheckRequestDto request
    ) {
        return ResponseEntity.ok(structureService.checkInvitationCode(id, request.getCode()));
    }

    // Vérifie et consomme le code : un code ne sert qu'à une seule inscription
    @PostMapping("/{id}/invitation-code/consume")
    public ResponseEntity<InvitationCodeCheckResponseDto> consumeInvitationCode(
            @PathVariable Long id,
            @Valid @RequestBody InvitationCodeCheckRequestDto request
    ) {
        log.info("Consommation d'un code d'invitation pour la structure {}", id);
        return ResponseEntity.ok(structureService.consumeInvitationCode(id, request.getCode()));
    }
}

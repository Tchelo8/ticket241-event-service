package com.tchelo.event_service_ticket241.controller;

import com.tchelo.event_service_ticket241.dto.request.StructureRequestDTO;
import com.tchelo.event_service_ticket241.dto.response.InvitationCodeResponseDto;
import com.tchelo.event_service_ticket241.dto.response.StructureResponseDTO;
import com.tchelo.event_service_ticket241.security.UserContext;
import com.tchelo.event_service_ticket241.service.StructureService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Lecture : tout utilisateur connecté. Écriture : voir les règles dans StructureService
// (création, vérification, suppression, codes d'invitation = admin Ticket241 ; modification = admin
// Ticket241 ou admin de la structure).
@RestController
@RequestMapping("/api/events/structures")
public class StructureController {

    private final StructureService structureService;

    public StructureController(StructureService structureService) {
        this.structureService = structureService;
    }

    @PostMapping("/create")
    public ResponseEntity<StructureResponseDTO> createStructure(
            @RequestBody StructureRequestDTO dto,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = true) String roles,
            @RequestHeader(value = "X-User-Structure-Id", required = false) String structureId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(structureService.createStructure(dto, UserContext.of(userId, roles, structureId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StructureResponseDTO> getStructureById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(structureService.getStructureById(id));
    }

    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<StructureResponseDTO> getStructureByUuid(
            @PathVariable String uuid
    ) {
        return ResponseEntity.ok(structureService.getStructureByUuid(uuid));
    }

    @GetMapping("/all")
    public ResponseEntity<List<StructureResponseDTO>> getAllStructures() {
        return ResponseEntity.ok(structureService.getAllStructures());
    }

    @PutMapping("/{id}")
    public ResponseEntity<StructureResponseDTO> updateStructure(
            @PathVariable Long id,
            @RequestBody StructureRequestDTO dto,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = true) String roles,
            @RequestHeader(value = "X-User-Structure-Id", required = false) String structureId
    ) {
        return ResponseEntity.ok(structureService.updateStructure(id, dto, UserContext.of(userId, roles, structureId)));
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<StructureResponseDTO> verifyStructure(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = true) String roles,
            @RequestHeader(value = "X-User-Structure-Id", required = false) String structureId
    ) {
        return ResponseEntity.ok(structureService.verifyStructure(id, UserContext.of(userId, roles, structureId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStructure(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = true) String roles,
            @RequestHeader(value = "X-User-Structure-Id", required = false) String structureId
    ) {
        structureService.deleteStructure(id, UserContext.of(userId, roles, structureId));
        return ResponseEntity.noContent().build();
    }

    // Génère le code à transmettre au futur admin de la structure (affiché une seule fois)
    @PostMapping("/{id}/invitation-code")
    public ResponseEntity<InvitationCodeResponseDto> generateInvitationCode(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = true) String roles,
            @RequestHeader(value = "X-User-Structure-Id", required = false) String structureId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(structureService.generateInvitationCode(id, UserContext.of(userId, roles, structureId)));
    }
}

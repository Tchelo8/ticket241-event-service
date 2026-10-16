package com.tchelo.event_service_ticket241.controller;

import com.tchelo.event_service_ticket241.dto.response.PublicStructureDto;
import com.tchelo.event_service_ticket241.service.PublicStructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Annuaire des organisateurs du site public (route publique de la gateway, sans JWT)
@RestController
@RequestMapping("/api/events/structures/public")
@RequiredArgsConstructor
public class PublicStructureController {

    private final PublicStructureService publicStructureService;

    @GetMapping
    public ResponseEntity<List<PublicStructureDto>> getVerifiedStructures() {
        return ResponseEntity.ok(publicStructureService.getVerifiedStructures());
    }
}

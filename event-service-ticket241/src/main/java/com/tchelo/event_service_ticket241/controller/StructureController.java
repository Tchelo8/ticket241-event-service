package com.tchelo.event_service_ticket241.controller;

import com.tchelo.event_service_ticket241.dto.request.StructureRequestDTO;
import com.tchelo.event_service_ticket241.dto.response.StructureResponseDTO;
import com.tchelo.event_service_ticket241.service.impl.StructureServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events/structures")
public class StructureController {

    private final StructureServiceImpl structureService;

    public StructureController(StructureServiceImpl structureService) {
        this.structureService = structureService;
    }

    @PostMapping("/create")
    public ResponseEntity<StructureResponseDTO> createStructure(
            @RequestBody StructureRequestDTO dto
    ) {
        return ResponseEntity.ok(structureService.createStructure(dto));
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
            @RequestBody StructureRequestDTO dto
    ) {
        return ResponseEntity.ok(structureService.updateStructure(id, dto));
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<StructureResponseDTO> verifyStructure(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(structureService.verifyStructure(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStructure(
            @PathVariable Long id
    ) {
        structureService.deleteStructure(id);
        return ResponseEntity.noContent().build();
    }
}
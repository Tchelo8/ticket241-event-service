package com.tchelo.event_service_ticket241.controller;

import com.tchelo.event_service_ticket241.dto.StructureRequestDTO;
import com.tchelo.event_service_ticket241.dto.StructureResponseDTO;
import com.tchelo.event_service_ticket241.service.impl.StructureServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/structures")
public class StructureController {
    private final StructureServiceImpl structureService;

    public StructureController(StructureServiceImpl structureService) {
        this.structureService = structureService;
    }
    @PostMapping("/create")
    public ResponseEntity<StructureResponseDTO> createStructure(
            @RequestBody  StructureRequestDTO dto
    ) {
        return ResponseEntity.ok(structureService.createStructure(dto));
    }
}

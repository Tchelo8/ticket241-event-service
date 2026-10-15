package com.tchelo.event_service_ticket241.service;

import com.tchelo.event_service_ticket241.dto.request.StructureRequestDTO;
import com.tchelo.event_service_ticket241.dto.response.StructureResponseDTO;

import java.util.List;

public interface StructureService {
    StructureResponseDTO createStructure(StructureRequestDTO dto);
    StructureResponseDTO getStructureById(Long id);
    StructureResponseDTO getStructureByUuid(String uuid);
    List<StructureResponseDTO> getAllStructures();
    StructureResponseDTO updateStructure(Long id, StructureRequestDTO dto);
    StructureResponseDTO verifyStructure(Long id);
    void deleteStructure(Long id);
}
package com.tchelo.event_service_ticket241.service;

import com.tchelo.event_service_ticket241.dto.StructureRequestDTO;
import com.tchelo.event_service_ticket241.dto.StructureResponseDTO;


public interface StructureService {
    StructureResponseDTO createStructure(StructureRequestDTO dto);
}

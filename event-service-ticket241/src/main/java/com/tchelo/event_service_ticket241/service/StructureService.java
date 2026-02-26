package com.tchelo.event_service_ticket241.service;

import com.tchelo.event_service_ticket241.dto.request.StructureRequestDTO;
import com.tchelo.event_service_ticket241.dto.response.StructureResponseDTO;


public interface StructureService {
    StructureResponseDTO createStructure(StructureRequestDTO dto);
}

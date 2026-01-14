package com.tchelo.event_service_ticket241.mapper;


import com.tchelo.event_service_ticket241.dto.StructureRequestDTO;
import com.tchelo.event_service_ticket241.dto.StructureResponseDTO;
import com.tchelo.event_service_ticket241.entity.Structure;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class StructureMapper {

    public Structure ResponseDTOToEntity(StructureRequestDTO dto) {
        Structure structure = new Structure();
        BeanUtils.copyProperties(dto, structure);
        return structure;
    }

    public StructureResponseDTO EntityToResponseDTO(Structure structure) {
        StructureResponseDTO response = new StructureResponseDTO();
        BeanUtils.copyProperties(structure, response);
        return response;
    }
}
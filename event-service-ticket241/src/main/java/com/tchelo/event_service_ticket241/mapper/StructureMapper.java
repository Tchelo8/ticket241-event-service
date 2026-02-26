package com.tchelo.event_service_ticket241.mapper;


import com.tchelo.event_service_ticket241.dto.request.StructureRequestDTO;
import com.tchelo.event_service_ticket241.dto.response.StructureResponseDTO;
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
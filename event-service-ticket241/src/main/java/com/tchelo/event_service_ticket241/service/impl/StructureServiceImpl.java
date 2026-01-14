package com.tchelo.event_service_ticket241.service.impl;

import com.tchelo.event_service_ticket241.dto.StructureRequestDTO;
import com.tchelo.event_service_ticket241.dto.StructureResponseDTO;
import com.tchelo.event_service_ticket241.entity.Structure;
import com.tchelo.event_service_ticket241.mapper.StructureMapper;
import com.tchelo.event_service_ticket241.repository.StructureRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class StructureServiceImpl {
    private final StructureRepository repository;
    private final StructureMapper mapper;

    public StructureServiceImpl(
            StructureRepository repository,
            StructureMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public StructureResponseDTO createStructure(StructureRequestDTO dto) {

        if (repository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Une structure avec cet email existe déjà");
        }

        Structure structure = mapper.ResponseDTOToEntity(dto);
        structure.setStatus(false);
        structure.setIsVerified(false);

        return mapper.EntityToResponseDTO(repository.save(structure));
    }
}

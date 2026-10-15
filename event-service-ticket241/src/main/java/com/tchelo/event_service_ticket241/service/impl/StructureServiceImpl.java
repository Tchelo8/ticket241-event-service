package com.tchelo.event_service_ticket241.service.impl;

import com.tchelo.event_service_ticket241.dto.request.StructureRequestDTO;
import com.tchelo.event_service_ticket241.dto.response.StructureResponseDTO;
import com.tchelo.event_service_ticket241.entity.Structure;
import com.tchelo.event_service_ticket241.mapper.StructureMapper;
import com.tchelo.event_service_ticket241.repository.StructureRepository;
import com.tchelo.event_service_ticket241.service.StructureService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StructureServiceImpl implements StructureService {

    private final StructureRepository repository;
    private final StructureMapper mapper;

    public StructureServiceImpl(StructureRepository repository, StructureMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public StructureResponseDTO createStructure(StructureRequestDTO dto) {
        if (repository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Une structure avec cet email existe déjà");
        }
        Structure structure = mapper.requestDTOToEntity(dto);
        structure.setStatus(false);
        structure.setIsVerified(false);
        return mapper.entityToResponseDTO(repository.save(structure));
    }

    @Override
    public StructureResponseDTO getStructureById(Long id) {
        Structure structure = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Structure introuvable avec l'id : " + id));
        return mapper.entityToResponseDTO(structure);
    }

    @Override
    public StructureResponseDTO getStructureByUuid(String uuid) {
        Structure structure = repository.findByUuid(uuid);
        if (structure == null) {
            throw new RuntimeException("Structure introuvable avec l'uuid : " + uuid);
        }
        return mapper.entityToResponseDTO(structure);
    }

    @Override
    public List<StructureResponseDTO> getAllStructures() {
        return repository.findAll()
                .stream()
                .map(mapper::entityToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StructureResponseDTO updateStructure(Long id, StructureRequestDTO dto) {
        Structure structure = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Structure introuvable avec l'id : " + id));
        mapper.updateEntityFromDTO(dto, structure);
        return mapper.entityToResponseDTO(repository.save(structure));
    }

    @Override
    public StructureResponseDTO verifyStructure(Long id) {
        Structure structure = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Structure introuvable avec l'id : " + id));
        structure.setIsVerified(true);
        structure.setStatus(true);
        return mapper.entityToResponseDTO(repository.save(structure));
    }

    @Override
    public void deleteStructure(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Structure introuvable avec l'id : " + id);
        }
        repository.deleteById(id);
    }
}
package com.tchelo.event_service_ticket241.service.impl;

import com.tchelo.event_service_ticket241.dto.response.PublicStructureDto;
import com.tchelo.event_service_ticket241.entity.Structure;
import com.tchelo.event_service_ticket241.entity.enums.EventStatus;
import com.tchelo.event_service_ticket241.repository.EventRepository;
import com.tchelo.event_service_ticket241.repository.StructureRepository;
import com.tchelo.event_service_ticket241.service.PublicStructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicStructureServiceImpl implements PublicStructureService {

    private final StructureRepository structureRepository;
    private final EventRepository eventRepository;

    @Override
    public List<PublicStructureDto> getVerifiedStructures() {
        LocalDateTime now = LocalDateTime.now();
        return structureRepository.findByIsVerifiedTrueOrderByNomAsc().stream()
                .map(structure -> toPublicDto(structure, now))
                .toList();
    }

    private PublicStructureDto toPublicDto(Structure structure, LocalDateTime now) {
        return PublicStructureDto.builder()
                .id(structure.getId())
                .uuid(structure.getUuid())
                .name(structure.getNom())
                .type(structure.getType())
                .city(structure.getLocation())
                .venue(structure.getLieu())
                .description(structure.getDescription())
                .logoUrl(structure.getLogoUrl())
                .upcomingEventCount(eventRepository.countUpcomingPublicEvents(structure.getId(), EventStatus.PUBLISHED, now))
                .build();
    }
}

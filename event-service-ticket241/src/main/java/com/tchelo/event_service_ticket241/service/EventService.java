package com.tchelo.event_service_ticket241.service;

import com.tchelo.event_service_ticket241.dto.request.CreateEventDto;
import com.tchelo.event_service_ticket241.dto.request.UpdateEventDto;
import com.tchelo.event_service_ticket241.dto.response.EventResponseDto;
import com.tchelo.event_service_ticket241.dto.response.EventSummaryDto;
import com.tchelo.event_service_ticket241.entity.enums.EventCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventService {


    EventResponseDto createEvent(CreateEventDto dto, String userId, String roles);

    EventResponseDto updateEvent(String uuid, UpdateEventDto dto, String userId, String roles);

    EventResponseDto getEventByUuid(String uuid);

    EventResponseDto getEventBySlug(String slug);

    // Liste tous les événements publics et publiés pour la page d'accueil par exemple
    Page<EventSummaryDto> getAllPublicEvents(Pageable pageable);

    Page<EventSummaryDto> getEventsByCategory(EventCategory category, Pageable pageable);

    Page<EventSummaryDto> getEventsByCity(Long cityId, Pageable pageable);

    EventResponseDto getEventById(Long id);

    // Recherche d'événements par mot-clé
    Page<EventSummaryDto> searchEvents(String keyword, Pageable pageable);

    // Liste les événements à venir
    Page<EventSummaryDto> getUpcomingEvents(Pageable pageable);

    //Liste les événements mis en avant
    Page<EventSummaryDto> getFeaturedEvents(Pageable pageable);

    Page<EventSummaryDto> getEventsByStructure(Long structureId, Pageable pageable);

    Page<EventSummaryDto> getEventsByCreator(String userId, Pageable pageable);

    // Supprime (soft delete) un événement
    void deleteEvent(String uuid, String userId, String roles);

    // Publie un événement (change le statut en PUBLISHED)
    EventResponseDto publishEvent(String uuid, String userId, String roles);

    // Annule un événement
    EventResponseDto cancelEvent(String uuid, String reason, String userId, String roles);

    // Incrémente le compteur de vues

    void incrementViewCount(String uuid);
}

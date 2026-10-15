package com.tchelo.event_service_ticket241.service;

import com.tchelo.event_service_ticket241.dto.request.CreateEventDto;
import com.tchelo.event_service_ticket241.dto.request.UpdateEventDto;
import com.tchelo.event_service_ticket241.dto.response.EventResponseDto;
import com.tchelo.event_service_ticket241.dto.response.EventSummaryDto;
import com.tchelo.event_service_ticket241.security.UserContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventService {

    // Création, modification, publication, annulation, suppression : admin Ticket241 ou admin de la
    // structure de l'événement (UserContext)

    EventResponseDto createEvent(CreateEventDto dto, UserContext user);

    EventResponseDto updateEvent(String uuid, UpdateEventDto dto, UserContext user);

    EventResponseDto getEventByUuid(String uuid);

    EventResponseDto getEventBySlug(String slug);

    // Détail back-office (sans compteur de vues) : admin ou admin de la structure
    EventResponseDto getEventBySlugForManagement(String slug, UserContext user);

    // Liste tous les événements publics et publiés pour la page d'accueil par exemple
    Page<EventSummaryDto> getAllPublicEvents(Pageable pageable);

    Page<EventSummaryDto> getEventsByCategory(String categoryName, Pageable pageable);

    Page<EventSummaryDto> getEventsByCity(Long cityId, Pageable pageable);

    EventResponseDto getEventById(Long id);

    // Recherche d'événements par mot-clé
    Page<EventSummaryDto> searchEvents(String keyword, Pageable pageable);

    // Liste les événements à venir
    Page<EventSummaryDto> getUpcomingEvents(Pageable pageable);

    //Liste les événements mis en avant
    Page<EventSummaryDto> getFeaturedEvents(Pageable pageable);

    // Back-office d'une structure (brouillons compris) : admin ou admin de la structure
    Page<EventResponseDto> getEventsByStructure(Long structureId, Pageable pageable, UserContext user);

    Page<EventSummaryDto> getEventsByCreator(String userId, Pageable pageable);

    // Supprime (soft delete) un événement
    void deleteEvent(String uuid, UserContext user);

    // Publie un événement (change le statut en PUBLISHED) ; structure vérifiée exigée hors admin
    EventResponseDto publishEvent(String uuid, UserContext user);

    // Annule un événement
    EventResponseDto cancelEvent(String uuid, String reason, UserContext user);

    // Incrémente le compteur de vues

    void incrementViewCount(String uuid);
}

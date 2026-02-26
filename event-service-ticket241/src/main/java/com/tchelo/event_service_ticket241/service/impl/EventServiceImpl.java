package com.tchelo.event_service_ticket241.service.impl;

import com.tchelo.event_service_ticket241.dto.request.CreateEventDto;
import com.tchelo.event_service_ticket241.dto.request.UpdateEventDto;
import com.tchelo.event_service_ticket241.dto.response.EventResponseDto;
import com.tchelo.event_service_ticket241.dto.response.EventSummaryDto;
import com.tchelo.event_service_ticket241.entity.City;
import com.tchelo.event_service_ticket241.entity.Event;
import com.tchelo.event_service_ticket241.entity.Structure;
import com.tchelo.event_service_ticket241.entity.TicketCategory;
import com.tchelo.event_service_ticket241.entity.enums.EventCategory;
import com.tchelo.event_service_ticket241.entity.enums.EventStatus;
import com.tchelo.event_service_ticket241.entity.enums.ValidationMode;
import com.tchelo.event_service_ticket241.exception.*;
import com.tchelo.event_service_ticket241.mapper.EventMapper;
import com.tchelo.event_service_ticket241.mapper.TicketCategoryMapper;
import com.tchelo.event_service_ticket241.repository.CityRepository;
import com.tchelo.event_service_ticket241.repository.EventRepository;
import com.tchelo.event_service_ticket241.repository.StructureRepository;
import com.tchelo.event_service_ticket241.repository.TicketCategoryRepository;
import com.tchelo.event_service_ticket241.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final StructureRepository structureRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final CityRepository cityRepository;
    private final EventMapper eventMapper;
    private final TicketCategoryMapper ticketCategoryMapper;

    @Override
    public EventResponseDto createEvent(CreateEventDto dto, String userId, String roles) {
        log.info("Création d'un événement par l'utilisateur sur Ticket241: {}", userId);

        // Validation des données métier
        validateEventDates(dto.getStartDate(), dto.getEndDate(), dto.getSaleStartDate(), dto.getSaleEndDate());
        validateTicketCategories(dto);

        // Vérifier que structureId n'est pas null
        if (dto.getStructureId() == null) {
            throw new InvalidEventDataException("L'ID de la structure est obligatoire");
        }

        //récupérer la structure
        Long structureId = dto.getStructureId();
        log.debug("Recherche de la structure avec l'ID : {}", structureId);

        Structure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new StructureNotFoundException(
                        "Structure non trouvée avec l'ID : " + structureId
                ));

        log.debug("Structure trouvée : {}", structure.getNom());

        //récupérer la ville
        Long cityId = dto.getCityId();
        log.debug("Recherche de la ville avec l'ID : {}", cityId);

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new CityNotFoundException(
                        "Ville non trouvée avec l'ID : " + cityId
                ));

        log.debug("Ville trouvée : {} - {}", city.getName(), city.getCountry());

        // Convertir DTO en entité
        Event event = eventMapper.toEntity(dto, structure, city);
        event.setCreatedBy(userId);
        event.setUpdatedBy(userId);

        // Calculer min/max prix depuis les catégories de tickets
        BigDecimal minPrice = dto.getTicketCategories().stream()
                .map(tc -> tc.getPrice())
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal maxPrice = dto.getTicketCategories().stream()
                .map(tc -> tc.getPrice())
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        event.setMinPrice(minPrice);
        event.setMaxPrice(maxPrice);

        // Si validation automatique et utilisateur admin → publier directement
        if (dto.getValidationMode() == ValidationMode.AUTO && roles.contains("ROLE_ADMIN")) {
            event.setEventStatus(EventStatus.PUBLISHED);
            event.setPublishedAt(LocalDateTime.now());
            event.setApprovedBy(userId);
            event.setApprovedAt(LocalDateTime.now());
        }

        // Sauvegarder l'événement
        Event savedEvent = eventRepository.save(event);

        // Ajouter les catégories de tickets
        dto.getTicketCategories().forEach(ticketCategoryDto -> {
            TicketCategory ticketCategory = ticketCategoryMapper.toEntity(ticketCategoryDto);
            savedEvent.addTicketCategory(ticketCategory);
        });

        // Sauvegarder les catégories
        Event finalEvent = eventRepository.save(savedEvent);

        city.setEventCount(city.getEventCount() + 1);
        cityRepository.save(city);

        log.info("Événement créé avec succès : UUID = {}", finalEvent.getUuid());

        return eventMapper.toResponseDto(finalEvent);
    }

    @Override
    public EventResponseDto updateEvent(String uuid, UpdateEventDto dto, String userId, String roles) {
        log.info("Mise à jour de l'événement {} par l'utilisateur {}", uuid, userId);

        // Récupérer l'événement
        Event event = eventRepository.findByUuid(uuid)
                .orElseThrow(() -> new EventNotFoundException(
                        "Événement non trouvé avec l'UUID : " + uuid
                ));

        // Vérifier les permissions
        checkUpdatePermission(event, userId, roles);

        // Validation des dates si modifiées
        if (dto.getStartDate() != null) {
            validateEventDates(dto.getStartDate(), dto.getEndDate(),
                    dto.getSaleStartDate(), dto.getSaleEndDate());
        }

        //si la ville est modifiée, d'abord vérifier qu'elle existe
        if (dto.getCityId() != null && !dto.getCityId().equals(event.getCity().getId())) {
            City newCity = cityRepository.findById(dto.getCityId())
                    .orElseThrow(() -> new CityNotFoundException(
                            "Ville non trouvée avec l'ID : " + dto.getCityId()
                    ));

            // Décrémenter l'ancienne ville
            City oldCity = event.getCity();
            oldCity.setEventCount(oldCity.getEventCount() - 1);
            cityRepository.save(oldCity);

            // Incrémenter la nouvelle ville
            newCity.setEventCount(newCity.getEventCount() + 1);
            cityRepository.save(newCity);

            // Assigner la nouvelle ville
            event.setCity(newCity);
        }

        // Mettre à jour l'entité
        eventMapper.updateEntity(event, dto);
        event.setUpdatedBy(userId);

        // Sauvegarder
        Event updatedEvent = eventRepository.save(event);

        log.info("Événement mis à jour avec succès : UUID = {}", uuid);

        return eventMapper.toResponseDto(updatedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponseDto getEventByUuid(String uuid) {
        Event event = eventRepository.findByUuid(uuid)
                .orElseThrow(() -> new EventNotFoundException(
                        "Événement non trouvé avec l'UUID : " + uuid
                ));

        return eventMapper.toResponseDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponseDto getEventBySlug(String slug) {
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new EventNotFoundException(
                        "Événement non trouvé avec le slug : " + slug
                ));

        return eventMapper.toResponseDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventSummaryDto> getAllPublicEvents(Pageable pageable) {
        return eventRepository.findPublicEvents(EventStatus.PUBLISHED, pageable)
                .map(eventMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventSummaryDto> getEventsByCategory(EventCategory category, Pageable pageable) {
        return eventRepository.findByCategoryAndIsPublicTrueAndIsActiveTrueAndStatus(
                category, EventStatus.PUBLISHED, pageable
        ).map(eventMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventSummaryDto> getEventsByCity(Long cityId, Pageable pageable) {
        return eventRepository.findByCityIdAndIsPublicTrueAndIsActiveTrueAndEventStatus(
                cityId, EventStatus.PUBLISHED, pageable
        ).map(eventMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponseDto getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(
                        "Événement non trouvé avec  l'ID : " + id
                ));

        return eventMapper.toResponseDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventSummaryDto> searchEvents(String keyword, Pageable pageable) {
        return eventRepository.searchEvents(keyword, EventStatus.PUBLISHED, pageable)
                .map(eventMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventSummaryDto> getUpcomingEvents(Pageable pageable) {
        return eventRepository.findUpcomingEvents(
                LocalDateTime.now(), EventStatus.PUBLISHED, pageable
        ).map(eventMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventSummaryDto> getFeaturedEvents(Pageable pageable) {
        return eventRepository.findFeaturedEvents(EventStatus.PUBLISHED, pageable)
                .map(eventMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventSummaryDto> getEventsByStructure(Long structureId, Pageable pageable) {
        return eventRepository.findByStructureIdAndIsActiveTrue(structureId, pageable)
                .map(eventMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventSummaryDto> getEventsByCreator(String userId, Pageable pageable) {
        return eventRepository.findByCreatedByAndIsActiveTrue(userId, pageable)
                .map(eventMapper::toSummaryDto);
    }

    @Override
    public void deleteEvent(String uuid, String userId, String roles) {
        log.info("Suppression de l'événement {} par l'utilisateur {}", uuid, userId);

        Event event = eventRepository.findByUuid(uuid)
                .orElseThrow(() -> new EventNotFoundException(
                        "Événement non trouvé avec l'UUID : " + uuid
                ));

        // Vérifier les permissions
        checkDeletePermission(event, userId, roles);

        // Soft delete
        event.setIsActive(false);
        event.setUpdatedBy(userId);
        eventRepository.save(event);

        //décrémenter le compteur d'événements de la ville lors du soft delete
        City city = event.getCity();
        if (city.getEventCount() > 0) {
            city.setEventCount(city.getEventCount() - 1);
            cityRepository.save(city);
        }

        log.info("Événement supprimé (soft delete) : UUID = {}", uuid);
    }

    @Override
    public EventResponseDto publishEvent(String uuid, String userId, String roles) {
        log.info("Publication de l'événement {} par l'utilisateur {}", uuid, userId);

        Event event = eventRepository.findByUuid(uuid)
                .orElseThrow(() -> new EventNotFoundException(
                        "Événement non trouvé avec l'UUID : " + uuid
                ));

        // Vérifier les permissions (admin ou créateur)
        checkUpdatePermission(event, userId, roles);

        // Valider que l'événement peut être publié
        if (event.getEventStatus() != EventStatus.DRAFT && event.getEventStatus() != EventStatus.PENDING_APPROVAL) {
            throw new InvalidEventDataException(
                    "Seuls les événements en brouillon ou en attente peuvent être publiés"
            );
        }

        // Publier
        event.setEventStatus(EventStatus.PUBLISHED);
        event.setPublishedAt(LocalDateTime.now());
        event.setApprovedBy(userId);
        event.setApprovedAt(LocalDateTime.now());
        event.setUpdatedBy(userId);

        Event publishedEvent = eventRepository.save(event);

        log.info("Événement publié avec succès : UUID = {}", uuid);

        return eventMapper.toResponseDto(publishedEvent);
    }

    @Override
    public EventResponseDto cancelEvent(String uuid, String reason, String userId, String roles) {
        log.info("Annulation de l'événement {} par l'utilisateur {}", uuid, userId);

        Event event = eventRepository.findByUuid(uuid)
                .orElseThrow(() -> new EventNotFoundException(
                        "Événement non trouvé avec l'UUID : " + uuid
                ));

        // Vérifier les permissions
        checkUpdatePermission(event, userId, roles);

        // Annuler
        event.setEventStatus(EventStatus.CANCELLED);
        event.setCancelledAt(LocalDateTime.now());
        event.setCancellationReason(reason);
        event.setUpdatedBy(userId);

        Event cancelledEvent = eventRepository.save(event);

        log.info("Événement annulé avec succès : UUID = {}", uuid);

        return eventMapper.toResponseDto(cancelledEvent);
    }

    @Override
    public void incrementViewCount(String uuid) {
        Event event = eventRepository.findByUuid(uuid)
                .orElseThrow(() -> new EventNotFoundException(
                        "Événement non trouvé avec l'UUID : " + uuid
                ));

        event.setViewCount(event.getViewCount() + 1);
        eventRepository.save(event);
    }

    // ============================================
    // Méthodes Privées de Validation
    // ============================================

    /**
     * Valide la cohérence des dates
     */
    private void validateEventDates(LocalDateTime startDate, LocalDateTime endDate,
                                    LocalDateTime saleStartDate, LocalDateTime saleEndDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new InvalidEventDataException(
                    "La date de fin doit être après la date de début"
            );
        }

        if (saleEndDate != null && saleEndDate.isAfter(startDate)) {
            throw new InvalidEventDataException(
                    "La date de fin de vente doit être avant le début de l'événement"
            );
        }

        if (saleStartDate != null && saleEndDate != null && saleEndDate.isBefore(saleStartDate)) {
            throw new InvalidEventDataException(
                    "La date de fin de vente doit être après la date de début de vente"
            );
        }
    }

    /**
     * Valide les catégories de tickets
     */
    private void validateTicketCategories(CreateEventDto dto) {
        if (dto.getTicketCategories().isEmpty()) {
            throw new InvalidEventDataException(
                    "Au moins une catégorie de ticket est obligatoire"
            );
        }

        // Vérifier que la somme des places des catégories = total de l'événement
        int totalCategorySeats = dto.getTicketCategories().stream()
                .mapToInt(tc -> tc.getTotalSeats())
                .sum();

        if (totalCategorySeats != dto.getTotalSeats()) {
            throw new InvalidEventDataException(
                    String.format("La somme des places des catégories (%d) doit être égale au total de l'événement (%d)",
                            totalCategorySeats, dto.getTotalSeats())
            );
        }
    }

    /**
     * Vérifie les permissions de mise à jour
     */
    private void checkUpdatePermission(Event event, String userId, String roles) {
        boolean isAdmin = roles.contains("ROLE_ADMIN");
        boolean isCreator = event.getCreatedBy().equals(userId);

        if (!isAdmin && !isCreator) {
            throw new UnauthorizedAccesException(
                    "Vous n'avez pas les permissions pour modifier cet événement"
            );
        }
    }

    /**
     * Vérifie les permissions de suppression
     */
    private void checkDeletePermission(Event event, String userId, String roles) {
        boolean isAdmin = roles.contains("ROLE_ADMIN");
        boolean isCreator = event.getCreatedBy().equals(userId);

        if (!isAdmin && !isCreator) {
            throw new UnauthorizedAccesException(
                    "Vous n'avez pas les permissions pour supprimer cet événement"
            );
        }

        // Vérifier qu'il n'y a pas de tickets vendus
        if (event.getSoldSeats() > 0) {
            throw new InvalidEventDataException(
                    "Impossible de supprimer un événement avec des tickets vendus"
            );
        }
    }
}

package com.tchelo.event_service_ticket241.controller;

import com.tchelo.event_service_ticket241.dto.request.CreateEventDto;
import com.tchelo.event_service_ticket241.dto.request.UpdateEventDto;
import com.tchelo.event_service_ticket241.dto.response.EventResponseDto;
import com.tchelo.event_service_ticket241.dto.response.EventSummaryDto;
import com.tchelo.event_service_ticket241.dto.response.TicketCategoryResponseDto;
import com.tchelo.event_service_ticket241.entity.enums.EventCategory;
import com.tchelo.event_service_ticket241.service.EventService;
import com.tchelo.event_service_ticket241.service.TicketCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;
    private final TicketCategoryService ticketCategoryService;


    // Endpoints Publics qui n'aurons pas de besoin du jwt et du filtre de la gateway pour être appellé
    @GetMapping("/all")
    public ResponseEntity<Page<EventSummaryDto>> getAllPublicEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        log.info("Récupération des événements publics - page: {}, size: {}", page, size);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<EventSummaryDto> events = eventService.getAllPublicEvents(pageable);

        log.info("{} événements récupérés", events.getTotalElements());

        return ResponseEntity.ok(events);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<EventResponseDto> getEventByUuid(@PathVariable String uuid) {
        log.info("Récupération de l'événement : {}", uuid);

        EventResponseDto event = eventService.getEventByUuid(uuid);

        eventService.incrementViewCount(uuid);

        return ResponseEntity.ok(event);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<EventResponseDto> getEventBySlug(@PathVariable String slug) {
        log.info("Récupération de l'événement par slug : {}", slug);

        EventResponseDto event = eventService.getEventBySlug(slug);

        eventService.incrementViewCount(event.getUuid());

        return ResponseEntity.ok(event);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<EventSummaryDto>> getEventsByCategory(
            @PathVariable EventCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Récupération des événements de catégorie : {}", category);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "startDate"));
        Page<EventSummaryDto> events = eventService.getEventsByCategory(category, pageable);

        return ResponseEntity.ok(events);
    }


    @GetMapping("/city/{cityId}")
    public ResponseEntity<Page<EventSummaryDto>> getEventsByCity(
            @PathVariable Long cityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Récupération des événements de la ville  : {}", cityId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "startDate"));
        Page<EventSummaryDto> events = eventService.getEventsByCity(cityId, pageable);

        return ResponseEntity.ok(events);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<EventSummaryDto>> searchEvents(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Recherche d'événements avec un mot-clé : {}", keyword);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "startDate"));
        Page<EventSummaryDto> events = eventService.searchEvents(keyword, pageable);

        log.info("{} événements trouvés", events.getTotalElements());

        return ResponseEntity.ok(events);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Page<EventSummaryDto>> getUpcomingEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Récupération des événements à venir");

        Pageable pageable = PageRequest.of(page, size);
        Page<EventSummaryDto> events = eventService.getUpcomingEvents(pageable);

        return ResponseEntity.ok(events);
    }

    @GetMapping("/featured")
    public ResponseEntity<Page<EventSummaryDto>> getFeaturedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        log.info("Récupération des événements mis en avant sur Ticket241");

        Pageable pageable = PageRequest.of(page, size);
        Page<EventSummaryDto> events = eventService.getFeaturedEvents(pageable);

        return ResponseEntity.ok(events);
    }

    // Endpoints Protégés c'est à dire sans authentification personne ne pourra les atteindre

    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(
            @Valid @RequestBody CreateEventDto dto,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = true) String roles
    ) {
        log.info("Création d'un événement par l'utilisateur : {}", userId);

        EventResponseDto createdEvent = eventService.createEvent(dto, userId, roles);

        log.info("Événement créé avec succès sut Ticket241: {}", createdEvent.getUuid());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable String uuid,
            @Valid @RequestBody UpdateEventDto dto,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = true) String roles
    ) {
        log.info("Mise à jour de l'événement {} par l'utilisateur {}", uuid, userId);

        EventResponseDto updatedEvent = eventService.updateEvent(uuid, dto, userId, roles);

        log.info("Événement mis à jour avec succès");

        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Map<String, String>> deleteEvent(
            @PathVariable String uuid,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = true) String roles
    ) {
        log.info("Suppression de l'événement {} par l'utilisateur {}", uuid, userId);

        eventService.deleteEvent(uuid, userId, roles);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Événement supprimé avec succès dela plateforme Ticket241");
        response.put("uuid", uuid);

        log.info("Événement supprimé avec succès");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{uuid}/publish")
    public ResponseEntity<EventResponseDto> publishEvent(
            @PathVariable String uuid,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = true) String roles
    ) {
        log.info("Publication de l'événement {} par l'utilisateur {}", uuid, userId);

        EventResponseDto publishedEvent = eventService.publishEvent(uuid, userId, roles);

        log.info("Événement publié avec succès");

        return ResponseEntity.ok(publishedEvent);
    }

    @PostMapping("/{uuid}/cancel")
    public ResponseEntity<EventResponseDto> cancelEvent(
            @PathVariable String uuid,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Roles", required = true) String roles
    ) {
        log.info("Annulation de l'événement {} par l'utilisateur {}", uuid, userId);

        String reason = body.getOrDefault("reason", "Aucune raison fournie");

        EventResponseDto cancelledEvent = eventService.cancelEvent(uuid, reason, userId, roles);

        log.info("Événement annulé avec succès");

        return ResponseEntity.ok(cancelledEvent);
    }

    @GetMapping("/structure/{structureId}")
    public ResponseEntity<Page<EventSummaryDto>> getEventsByStructure(
            @PathVariable Long structureId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Récupération des événements de la structure : {}", structureId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<EventSummaryDto> events = eventService.getEventsByStructure(structureId, pageable);

        return ResponseEntity.ok(events);
    }

    @GetMapping("/my-events")
    public ResponseEntity<Page<EventSummaryDto>> getMyEvents(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Récupération des événements de l'utilisateur : {}", userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<EventSummaryDto> events = eventService.getEventsByCreator(userId, pageable);

        return ResponseEntity.ok(events);
    }


    // endpoints pour ticket service et la communication via kafka

    // Récupérer un événement par ID pour le service ticket
    @GetMapping("/id/{id}")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable Long id) {
        log.info("Requête récupération événement par ID : {}", id);

        EventResponseDto response = eventService.getEventById(id);

        return ResponseEntity.ok(response);
    }

    // Récupérer les détails d'une catégorie de ticket
    @GetMapping("/{eventId}/categories/{categoryId}")
    public ResponseEntity<TicketCategoryResponseDto> getTicketCategory(
            @PathVariable Long eventId,
            @PathVariable Long categoryId
    ) {
        log.info("Requête catégorie {} pour événement {}", categoryId, eventId);

        TicketCategoryResponseDto response = ticketCategoryService.getCategoryById(categoryId);

        return ResponseEntity.ok(response);
    }

    // Vérifier la disponibilité des places
    @GetMapping("/{eventId}/categories/{categoryId}/availability")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable Long eventId,
            @PathVariable Long categoryId,
            @RequestParam Integer quantity
    ) {
        log.info("Vérification disponibilité : {} places pour catégorie {}", quantity, categoryId);

        Boolean isAvailable = ticketCategoryService.checkAvailability(categoryId, quantity);

        return ResponseEntity.ok(isAvailable);
    }

    // Décrémenter les places disponibles route configuréé à être appelé qu'après paiement confirmé
    @PostMapping("/{eventId}/categories/{categoryId}/decrease-seats")
    public ResponseEntity<Void> decreaseAvailableSeats(
            @PathVariable Long eventId,
            @PathVariable Long categoryId,
            @RequestParam Integer quantity
    ) {
        log.info("Décrémentation de {} places pour catégorie {}", quantity, categoryId);

        ticketCategoryService.decreaseAvailableSeats(categoryId, quantity);

        return ResponseEntity.ok().build();
    }

    // Incrémenter les places disponibles (te config à être appelé après annulation 'un ticket ou d'une reservation
    @PostMapping("/{eventId}/categories/{categoryId}/increase-seats")
    public ResponseEntity<Void> increaseAvailableSeats(
            @PathVariable Long eventId,
            @PathVariable Long categoryId,
            @RequestParam Integer quantity
    ) {
        log.info("Incrémentation de {} places pour catégorie {}", quantity, categoryId);

        ticketCategoryService.increaseAvailableSeats(categoryId, quantity);

        return ResponseEntity.ok().build();
    }
}
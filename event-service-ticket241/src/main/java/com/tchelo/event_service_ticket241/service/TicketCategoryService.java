package com.tchelo.event_service_ticket241.service;

import com.tchelo.event_service_ticket241.dto.response.TicketCategoryResponseDto;

// Lecture des catégories de billets. Les mouvements de places (réserver, vendre, libérer) ne
// passent que par SeatReservationService, piloté par les messages Kafka de ticket-service.
public interface TicketCategoryService {

    TicketCategoryResponseDto getCategoryById(Long categoryId);

    // Catégorie d'un événement donné : 404 si elle n'appartient pas à cet événement
    TicketCategoryResponseDto getCategoryForEvent(Long eventId, Long categoryId);

    Boolean checkAvailability(Long eventId, Long categoryId, Integer quantity);
}

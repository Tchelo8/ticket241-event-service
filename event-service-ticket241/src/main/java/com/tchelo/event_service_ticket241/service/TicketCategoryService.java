package com.tchelo.event_service_ticket241.service;

import com.tchelo.event_service_ticket241.dto.response.TicketCategoryResponseDto;

public interface TicketCategoryService {

    TicketCategoryResponseDto getCategoryById(Long categoryId);

    Boolean checkAvailability(Long categoryId, Integer quantity);

    void decreaseAvailableSeats(Long categoryId, Integer quantity);

    void increaseAvailableSeats(Long categoryId, Integer quantity);

    void reserveSeats(Long categoryId, Integer quantity);

    void cancelReservation(Long categoryId, Integer quantity);
}
